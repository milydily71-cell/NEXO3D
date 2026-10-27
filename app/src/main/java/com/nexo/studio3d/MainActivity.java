package com.nexo.studio3d;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final int PICK_MODEL=41, SAVE_SCENE=42;
    private static final int MAX_ASSET_BYTES=32*1024*1024;
    private SceneView scene;
    private TextView status;
    private boolean gridEnabled=true;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        try { NativeScene.nativeReset(); }
        catch (UnsatisfiedLinkError e) {
            new AlertDialog.Builder(this).setTitle("Native engine failed")
                .setMessage("NEXO C++ library could not load. Reinstall the latest APK.")
                .setPositiveButton("Close",null).show();
            return;
        }
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.rgb(16,19,28));
        LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(12,6,12,6);
        TextView title=new TextView(this); title.setText("NEXO  /  STUDIO 3D"); title.setTextColor(Color.WHITE); title.setTextSize(17); title.setTypeface(null,1);
        top.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        Button importButton=button("Import GLB"); importButton.setOnClickListener(v->chooseModel()); top.addView(importButton); root.addView(top);
        scene=new SceneView(); root.addView(scene,new LinearLayout.LayoutParams(-1,0,1));
        HorizontalScrollView scroll=new HorizontalScrollView(this); scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout tools=new LinearLayout(this); tools.setPadding(6,6,6,6); tools.setGravity(Gravity.CENTER_VERTICAL); tools.setBackgroundColor(Color.rgb(24,28,40));
        String[] names={"+ Cube","Remove","Undo","Redo","Grid","Save Scene"};
        for(String name:names){Button b=button(name);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,46);lp.setMargins(3,0,3,0);tools.addView(b,lp);b.setOnClickListener(v->handleTool(name));}
        scroll.addView(tools);root.addView(scroll);
        status=new TextView(this);status.setTextColor(Color.rgb(190,199,220));status.setTextSize(12);status.setPadding(12,7,12,7);root.addView(status);
        setContentView(root);refreshStatus("Native scene ready");
    }
    private Button button(String label){Button b=new Button(this);b.setText(label);b.setTextSize(11);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(55,66,96)));return b;}
    private void handleTool(String name){
        switch(name){
            case "+ Cube": NativeScene.nativeAddCube();syncRenderer();refreshStatus("Cube added");break;
            case "Remove": if(NativeScene.nativeRemoveLast()){syncRenderer();refreshStatus("Last object removed");}else toast("Scene is already empty");break;
            case "Undo": if(NativeScene.nativeUndo()){syncRenderer();refreshStatus("Undo complete");}else toast("Nothing to undo");break;
            case "Redo": if(NativeScene.nativeRedo()){syncRenderer();refreshStatus("Redo complete");}else toast("Nothing to redo");break;
            case "Grid": gridEnabled=!gridEnabled;final boolean enabled=gridEnabled;scene.queueEvent(()->scene.renderer.setGridEnabled(enabled));refreshStatus(enabled?"Editor grid enabled":"Editor grid hidden");break;
            case "Save Scene": saveScene();break;
        }
    }
    private void syncRenderer(){int count=NativeScene.count();scene.queueEvent(()->scene.renderer.setObjectCount(count));}
    private void refreshStatus(String message){status.setText(message+"  •  Objects: "+NativeScene.count());}
    private void toast(String message){Toast.makeText(this,message,Toast.LENGTH_SHORT).show();}
    private void chooseModel(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,PICK_MODEL);}
    private void saveScene(){Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,"nexo-scene.json");startActivityForResult(i,SAVE_SCENE);}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();
        if(requestCode==SAVE_SCENE){try(java.io.OutputStream out=getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new java.io.IOException("Unable to open destination");out.write(NativeScene.nativeSerialize().getBytes(StandardCharsets.UTF_8));toast("Scene JSON saved");}catch(Exception e){toast("Save failed: "+e.getMessage());}return;}
        if(requestCode!=PICK_MODEL)return;
        String name=uri.getLastPathSegment();if(name==null)name="selected asset";
        try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
        try(InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream out=new ByteArrayOutputStream()){
            if(in==null)throw new java.io.IOException("Could not open selected asset");byte[] buffer=new byte[8192];int total=0,n;
            while((n=in.read(buffer))!=-1){total+=n;if(total>MAX_ASSET_BYTES)throw new java.io.IOException("File exceeds 32 MiB safety limit");out.write(buffer,0,n);}
            String result=NativeScene.nativeInspectGltf(out.toByteArray());status.setText(name+" • "+result);
            new AlertDialog.Builder(this).setTitle("glTF asset inspection").setMessage(result+"\n\nThis validates the GLB/glTF 2.0 container only. Full mesh, texture, skin and animation rendering is not yet connected to the viewport.").setPositiveButton("OK",null).show();
        }catch(Exception e){new AlertDialog.Builder(this).setTitle("Import inspection failed").setMessage(e.getMessage()==null?"Unable to read asset":e.getMessage()).setPositiveButton("OK",null).show();}
    }
    @Override protected void onPause(){if(scene!=null)scene.onPause();super.onPause();}
    @Override protected void onResume(){super.onResume();if(scene!=null)scene.onResume();}

    private final class SceneView extends GLSurfaceView {
        final SceneRenderer renderer; float previousX,previousY,previousDistance;
        SceneView(){super(MainActivity.this);setEGLContextClientVersion(2);setPreserveEGLContextOnPause(false);renderer=new SceneRenderer();setRenderer(renderer);setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);}
        private float distance(MotionEvent e){if(e.getPointerCount()<2)return 0;float dx=e.getX(0)-e.getX(1),dy=e.getY(0)-e.getY(1);return (float)Math.sqrt(dx*dx+dy*dy);}
        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getPointerCount()>=2){float d=distance(e);if(e.getActionMasked()==MotionEvent.ACTION_POINTER_DOWN)previousDistance=d;else if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&previousDistance>0&&d>0){float factor=previousDistance/d;queueEvent(()->renderer.zoomBy(factor));previousDistance=d;}return true;}
            float x=e.getX(),y=e.getY();if(e.getActionMasked()==MotionEvent.ACTION_DOWN){previousX=x;previousY=y;previousDistance=0;return true;}
            if(e.getActionMasked()==MotionEvent.ACTION_MOVE){final float dx=x-previousX,dy=y-previousY;queueEvent(()->renderer.orbit(dx,dy));previousX=x;previousY=y;return true;}
            if(e.getActionMasked()==MotionEvent.ACTION_POINTER_UP)previousDistance=0;return true;
        }
    }
}
