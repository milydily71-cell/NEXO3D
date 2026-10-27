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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {
    private static final int PICK_MODEL = 41;
    private SceneView scene;
    private TextView status;
    private int primitiveCount = 1;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(16, 19, 28));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(14, 8, 14, 8);
        TextView title = new TextView(this);
        title.setText("NEXO  /  STUDIO 3D");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setTypeface(null, 1);
        top.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        Button importButton = button("Import GLB");
        importButton.setOnClickListener(v -> chooseModel());
        top.addView(importButton);
        root.addView(top);

        scene = new SceneView();
        root.addView(scene, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout tools = new LinearLayout(this);
        tools.setPadding(8, 7, 8, 7);
        tools.setGravity(Gravity.CENTER);
        tools.setBackgroundColor(Color.rgb(24, 28, 40));
        String[] names = {"＋ Cube", "Model", "Rig", "Animate", "Render"};
        for (String name : names) {
            Button b = button(name);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 48, 1);
            lp.setMargins(3, 0, 3, 0);
            tools.addView(b, lp);
            b.setOnClickListener(v -> handleTool(name));
        }
        root.addView(tools);

        status = new TextView(this);
        status.setText("Scene: Cube  •  Drag to orbit  •  Pinch to zoom");
        status.setTextColor(Color.rgb(190, 199, 220));
        status.setTextSize(12);
        status.setPadding(12, 8, 12, 8);
        root.addView(status);
        setContentView(root);
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(55, 66, 96)));
        return b;
    }

    private void handleTool(String name) {
        switch (name) {
            case "＋ Cube":
                primitiveCount++;
                scene.queueEvent(() -> scene.renderer.setObjectCount(primitiveCount));
                status.setText("Scene: " + primitiveCount + " primitive cubes");
                break;
            case "Model":
                new AlertDialog.Builder(this).setTitle("Model tools")
                    .setMessage("Starter tools: primitive scene objects and orbit controls. Mesh editing (vertices, edges, faces), extrude and UV tools are planned for the next milestones.")
                    .setPositiveButton("OK", null).show();
                break;
            case "Rig":
                new AlertDialog.Builder(this).setTitle("Skeleton & rigging")
                    .setMessage("Bone hierarchy, skin weights and skeletal deformation are planned features; they are not implemented in this starter build.")
                    .setPositiveButton("OK", null).show();
                break;
            case "Animate":
                new AlertDialog.Builder(this).setTitle("Animation timeline")
                    .setMessage("Keyframe timeline, animation tracks and skeletal playback are planned for the next milestone.")
                    .setPositiveButton("OK", null).show();
                break;
            case "Render":
                new AlertDialog.Builder(this).setTitle("Render")
                    .setMessage("This build shows a real-time OpenGL ES viewport. Offline rendering, PBR materials, shadows and export are not implemented yet.")
                    .setPositiveButton("OK", null).show();
                break;
        }
    }

    private void chooseModel() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_MODEL);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_MODEL || resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;
        String name = uri.getLastPathSegment();
        if (name == null) name = "selected file";
        if (!name.toLowerCase().contains(".glb") && !name.toLowerCase().contains(".gltf")) {
            Toast.makeText(this, "Choose a .glb or .gltf file", Toast.LENGTH_LONG).show();
            return;
        }
        // Keep access to the selected document for the upcoming asset-loader milestone.
        try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
        catch (Exception ignored) {}
        status.setText("Selected model: " + name + " (loader integration pending)");
        new AlertDialog.Builder(this).setTitle("Model selected")
            .setMessage("The file was selected successfully. Rendering imported GLB/glTF meshes, textures, rigs and animations requires the asset-loader milestone.")
            .setPositiveButton("OK", null).show();
    }

    @Override protected void onPause() { super.onPause(); if (scene != null) scene.onPause(); }
    @Override protected void onResume() { super.onResume(); if (scene != null) scene.onResume(); }

    private final class SceneView extends GLSurfaceView {
        final SceneRenderer renderer;
        float previousX, previousY;
        SceneView() {
            super(MainActivity.this);
            setEGLContextClientVersion(2);
            renderer = new SceneRenderer();
            setRenderer(renderer);
            setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        }
        @Override public boolean onTouchEvent(MotionEvent e) {
            if (e.getPointerCount() >= 2) return true;
            float x = e.getX(), y = e.getY();
            if (e.getAction() == MotionEvent.ACTION_DOWN) { previousX=x; previousY=y; return true; }
            if (e.getAction() == MotionEvent.ACTION_MOVE) {
                final float dx=x-previousX, dy=y-previousY;
                queueEvent(() -> renderer.orbit(dx, dy));
                previousX=x; previousY=y; return true;
            }
            return true;
        }
    }
}
