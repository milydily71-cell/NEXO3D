package com.nexo.studio3d;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

final class SceneRenderer implements GLSurfaceView.Renderer {
    private static final String VERT="attribute vec3 aPosition; attribute vec3 aColor; uniform mat4 uMvp; uniform vec3 uLightDirection; uniform float uAmbient; varying vec3 vColor; void main(){gl_Position=uMvp*vec4(aPosition,1.0);float diffuse=max(0.0,dot(normalize(aPosition),normalize(-uLightDirection)));vColor=aColor*(uAmbient+(1.0-uAmbient)*diffuse);}";
    private static final String FRAG="precision mediump float; varying vec3 vColor; void main(){gl_FragColor=vec4(vColor,1.0);}";
    private final float[] cube={
        -1,-1,-1,.9f,.25f,.25f,1,-1,-1,.9f,.25f,.25f,1,1,-1,.9f,.25f,.25f,
        -1,-1,-1,.9f,.25f,.25f,1,1,-1,.9f,.25f,.25f,-1,1,-1,.9f,.25f,.25f,
        -1,-1,1,.25f,.75f,1,1,1,1,.25f,.75f,1,1,-1,1,.25f,.75f,1,
        -1,-1,1,.25f,.75f,1,-1,1,1,.25f,.75f,1,1,1,1,.25f,.75f,1,
        -1,-1,-1,.35f,.85f,.45f,-1,1,-1,.35f,.85f,.45f,-1,1,1,.35f,.85f,.45f,
        -1,-1,-1,.35f,.85f,.45f,-1,1,1,.35f,.85f,.45f,-1,-1,1,.35f,.85f,.45f,
        1,-1,-1,1,.7f,.25f,1,-1,1,1,.7f,.25f,1,1,1,1,.7f,.25f,
        1,-1,-1,1,.7f,.25f,1,1,1,1,.7f,.25f,1,1,-1,1,.7f,.25f,
        -1,1,-1,.55f,.45f,1,1,1,-1,.55f,.45f,1,1,1,1,.55f,.45f,1,
        -1,1,-1,.55f,.45f,1,1,1,1,.55f,.45f,1,-1,1,1,.55f,.45f,1,
        -1,-1,-1,.95f,.45f,.75f,1,-1,1,.95f,.45f,.75f,1,-1,-1,.95f,.45f,.75f,
        -1,-1,-1,.95f,.45f,.75f,-1,-1,1,.95f,.45f,.75f,1,-1,1,.95f,.45f,.75f
    };
    private FloatBuffer cubeData,gridData,axisData,importedData;
    private int gridVertices,importedVertices,program,posLoc,colorLoc,mvpLoc,lightLoc,ambientLoc,width=1,height=1,count=1;
    private boolean showGrid=true;
    private float yaw=35,pitch=22,zoom=8.5f;
    private final float[] projection=new float[16],view=new float[16],model=new float[16],vp=new float[16],mvp=new float[16];

    void setObjectCount(int n){count=Math.max(0,Math.min(128,n));}
    void setImportedMesh(float[] packed){if(packed==null||packed.length<18||packed.length%6!=0){importedData=null;importedVertices=0;return;}importedData=buffer(packed);importedVertices=packed.length/6;}
    void setGridEnabled(boolean enabled){showGrid=enabled;}
    void setCameraMode(int mode){cameraMode=Math.max(0,Math.min(4,mode));switch(cameraMode){case 1:yaw=0;pitch=0;zoom=8;break;case 2:yaw=0;pitch=88;zoom=8;break;case 3:yaw=90;pitch=0;zoom=8;break;case 4:yaw=35;pitch=22;zoom=8;break;default:break;}}
    void setDirectionalLight(boolean enabled){directionalLight=enabled;}
    void setLightProbe(boolean enabled){lightProbe=enabled;}
    void orbit(float dx,float dy){yaw+=dx*.6f;pitch=Math.max(-80,Math.min(80,pitch+dy*.45f));}
    void zoomBy(float factor){if(factor>0&&Float.isFinite(factor))zoom=Math.max(2.5f,Math.min(30f,zoom*factor));}

    @Override public void onSurfaceCreated(GL10 gl,EGLConfig config){
        GLES20.glClearColor(.055f,.07f,.105f,1f);GLES20.glEnable(GLES20.GL_DEPTH_TEST);GLES20.glDisable(GLES20.GL_CULL_FACE);
        cubeData=buffer(cube);
        float[] lines=new float[41*4*6];int k=0;
        for(int i=-20;i<=20;i++){float c=(i%5==0)?.28f:.14f;k=putLine(lines,k,-20,-1.05f,i,20,-1.05f,i,c,c,c);k=putLine(lines,k,i,-1.05f,-20,i,-1.05f,20,c,c,c);}
        gridVertices=k/6;gridData=buffer(lines);axisData=buffer(new float[]{0,0,0,1,.22f,.22f,2,0,0,1,.22f,.22f,0,0,0,.25f,1,.35f,0,2,0,.25f,1,.35f,0,0,0,.25f,.65f,1,0,0,2,.25f,.65f,1});
        int vs=shader(GLES20.GL_VERTEX_SHADER,VERT),fs=shader(GLES20.GL_FRAGMENT_SHADER,FRAG);
        program=GLES20.glCreateProgram();GLES20.glAttachShader(program,vs);GLES20.glAttachShader(program,fs);GLES20.glLinkProgram(program);
        int[] ok=new int[1];GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,ok,0);
        if(ok[0]==0){android.util.Log.e("NEXO3D","Shader link failed: "+GLES20.glGetProgramInfoLog(program));GLES20.glDeleteProgram(program);program=0;}
        else{posLoc=GLES20.glGetAttribLocation(program,"aPosition");colorLoc=GLES20.glGetAttribLocation(program,"aColor");mvpLoc=GLES20.glGetUniformLocation(program,"uMvp");lightLoc=GLES20.glGetUniformLocation(program,"uLightDirection");ambientLoc=GLES20.glGetUniformLocation(program,"uAmbient");}
        GLES20.glDeleteShader(vs);GLES20.glDeleteShader(fs);
    }
    private int putLine(float[] a,int k,float x1,float y1,float z1,float x2,float y2,float z2,float r,float g,float b){float[] v={x1,y1,z1,r,g,b,x2,y2,z2,r,g,b};System.arraycopy(v,0,a,k,v.length);return k+v.length;}
    private FloatBuffer buffer(float[] values){ByteBuffer bb=ByteBuffer.allocateDirect(values.length*4).order(ByteOrder.nativeOrder());FloatBuffer b=bb.asFloatBuffer();b.put(values).position(0);return b;}
    private int shader(int type,String source){int s=GLES20.glCreateShader(type);GLES20.glShaderSource(s,source);GLES20.glCompileShader(s);int[] ok=new int[1];GLES20.glGetShaderiv(s,GLES20.GL_COMPILE_STATUS,ok,0);if(ok[0]==0)android.util.Log.e("NEXO3D","Shader compile failed: "+GLES20.glGetShaderInfoLog(s));return s;}
    @Override public void onSurfaceChanged(GL10 gl,int w,int h){width=Math.max(1,w);height=Math.max(1,h);GLES20.glViewport(0,0,width,height);Matrix.perspectiveM(projection,0,45f,(float)width/height,.1f,150f);}
    @Override public void onDrawFrame(GL10 gl){
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);if(program==0)return;
        float yr=(float)Math.toRadians(yaw),pr=(float)Math.toRadians(pitch);
        float cx=(float)(zoom*Math.cos(pr)*Math.sin(yr)),cy=(float)(zoom*Math.sin(pr)),cz=(float)(zoom*Math.cos(pr)*Math.cos(yr));
        Matrix.setLookAtM(view,0,cx,cy,cz,0,0,0,0,1,0);Matrix.multiplyMM(vp,0,projection,0,view,0);GLES20.glUseProgram(program);
        if(showGrid){Matrix.setIdentityM(model,0);Matrix.multiplyMM(mvp,0,vp,0,model,0);GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);bind(gridData);GLES20.glDrawArrays(GLES20.GL_LINES,0,gridVertices);unbind();}
        Matrix.setIdentityM(model,0);Matrix.multiplyMM(mvp,0,vp,0,model,0);GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);bind(axisData);GLES20.glLineWidth(3f);GLES20.glDrawArrays(GLES20.GL_LINES,0,6);unbind();GLES20.glLineWidth(1f);
        if(importedData!=null){Matrix.setIdentityM(model,0);Matrix.multiplyMM(mvp,0,vp,0,model,0);GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);bind(importedData);GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,importedVertices);unbind();}
        else {bind(cubeData);for(int i=0;i<count;i++){Matrix.setIdentityM(model,0);float x=(i%8-3.5f)*2.35f,z=(i/8)*2.35f;Matrix.translateM(model,0,x,0,z);Matrix.scaleM(model,0,.55f,.55f,.55f);Matrix.multiplyMM(mvp,0,vp,0,model,0);GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,36);}unbind();}
    }
    private void bind(FloatBuffer b){b.position(0);GLES20.glVertexAttribPointer(posLoc,3,GLES20.GL_FLOAT,false,24,b);GLES20.glEnableVertexAttribArray(posLoc);b.position(3);GLES20.glVertexAttribPointer(colorLoc,3,GLES20.GL_FLOAT,false,24,b);GLES20.glEnableVertexAttribArray(colorLoc);}
    private void unbind(){GLES20.glDisableVertexAttribArray(posLoc);GLES20.glDisableVertexAttribArray(colorLoc);}
}
