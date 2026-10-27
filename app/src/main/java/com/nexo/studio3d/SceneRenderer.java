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
    private static final String VERT = "attribute vec3 aPosition; attribute vec3 aColor; uniform mat4 uMvp; varying vec3 vColor; void main(){ gl_Position=uMvp*vec4(aPosition,1.0); vColor=aColor; }";
    private static final String FRAG = "precision mediump float; varying vec3 vColor; void main(){ gl_FragColor=vec4(vColor,1.0); }";
    private final float[] vertices = {
        -1,-1,-1, 0.9f,0.25f,0.25f,  1,-1,-1, 0.9f,0.25f,0.25f,  1,1,-1, 0.9f,0.25f,0.25f,
        -1,-1,-1, 0.9f,0.25f,0.25f,  1,1,-1, 0.9f,0.25f,0.25f, -1,1,-1, 0.9f,0.25f,0.25f,
        -1,-1,1, 0.25f,0.75f,1,  1,1,1, 0.25f,0.75f,1,  1,-1,1, 0.25f,0.75f,1,
        -1,-1,1, 0.25f,0.75f,1, -1,1,1, 0.25f,0.75f,1, 1,1,1, 0.25f,0.75f,1,
        -1,-1,-1, 0.35f,0.85f,0.45f, -1,1,-1, 0.35f,0.85f,0.45f, -1,1,1, 0.35f,0.85f,0.45f,
        -1,-1,-1, 0.35f,0.85f,0.45f, -1,1,1, 0.35f,0.85f,0.45f, -1,-1,1, 0.35f,0.85f,0.45f,
        1,-1,-1, 1,0.7f,0.25f, 1,-1,1, 1,0.7f,0.25f, 1,1,1, 1,0.7f,0.25f,
        1,-1,-1, 1,0.7f,0.25f, 1,1,1, 1,0.7f,0.25f, 1,1,-1, 1,0.7f,0.25f,
        -1,1,-1, 0.55f,0.45f,1, 1,1,-1, 0.55f,0.45f,1, 1,1,1, 0.55f,0.45f,1,
        -1,1,-1, 0.55f,0.45f,1, 1,1,1, 0.55f,0.45f,1, -1,1,1, 0.55f,0.45f,1,
        -1,-1,-1, 0.95f,0.45f,0.75f, 1,-1,1, 0.95f,0.45f,0.75f, 1,-1,-1, 0.95f,0.45f,0.75f,
        -1,-1,-1, 0.95f,0.45f,0.75f, -1,-1,1, 0.95f,0.45f,0.75f, 1,-1,1, 0.95f,0.45f,0.75f
    };
    private FloatBuffer data;
    private int program, posLoc, colorLoc, mvpLoc;
    private int width=1, height=1, count=1;
    private float yaw=35, pitch=22, zoom=6.5f;
    private final float[] projection=new float[16], view=new float[16], model=new float[16], vp=new float[16], mvp=new float[16];

    void setObjectCount(int n) { count=Math.max(1,Math.min(12,n)); }
    void orbit(float dx,float dy) { yaw += dx*0.6f; pitch=Math.max(-80,Math.min(80,pitch+dy*0.45f)); }

    @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.055f,0.07f,0.105f,1f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        ByteBuffer bb=ByteBuffer.allocateDirect(vertices.length*4).order(ByteOrder.nativeOrder());
        data=bb.asFloatBuffer(); data.put(vertices).position(0);
        int vs=shader(GLES20.GL_VERTEX_SHADER,VERT), fs=shader(GLES20.GL_FRAGMENT_SHADER,FRAG);
        program=GLES20.glCreateProgram(); GLES20.glAttachShader(program,vs); GLES20.glAttachShader(program,fs); GLES20.glLinkProgram(program);
        posLoc=GLES20.glGetAttribLocation(program,"aPosition"); colorLoc=GLES20.glGetAttribLocation(program,"aColor"); mvpLoc=GLES20.glGetUniformLocation(program,"uMvp");
    }
    private int shader(int type,String source) { int s=GLES20.glCreateShader(type); GLES20.glShaderSource(s,source); GLES20.glCompileShader(s); return s; }
    @Override public void onSurfaceChanged(GL10 gl,int w,int h) { width=Math.max(1,w); height=Math.max(1,h); GLES20.glViewport(0,0,width,height); Matrix.perspectiveM(projection,0,45f,(float)width/height,0.1f,100f); }
    @Override public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
        float yr=(float)Math.toRadians(yaw), pr=(float)Math.toRadians(pitch);
        float cx=(float)(zoom*Math.cos(pr)*Math.sin(yr)), cy=(float)(zoom*Math.sin(pr)), cz=(float)(zoom*Math.cos(pr)*Math.cos(yr));
        Matrix.setLookAtM(view,0,cx,cy,cz,0,0,0,0,1,0);
        Matrix.multiplyMM(vp,0,projection,0,view,0);
        GLES20.glUseProgram(program);
        data.position(0); GLES20.glVertexAttribPointer(posLoc,3,GLES20.GL_FLOAT,false,24,data); GLES20.glEnableVertexAttribArray(posLoc);
        data.position(3); GLES20.glVertexAttribPointer(colorLoc,3,GLES20.GL_FLOAT,false,24,data); GLES20.glEnableVertexAttribArray(colorLoc);
        for(int i=0;i<count;i++) {
            Matrix.setIdentityM(model,0);
            float x=(i%4-1.5f)*2.35f, z=(i/4)*2.35f;
            Matrix.translateM(model,0,x,0,z);
            Matrix.scaleM(model,0,0.55f,0.55f,0.55f);
            Matrix.multiplyMM(mvp,0,vp,0,model,0);
            GLES20.glUniformMatrix4fv(mvpLoc,1,false,mvp,0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,36);
        }
        GLES20.glDisableVertexAttribArray(posLoc); GLES20.glDisableVertexAttribArray(colorLoc);
    }
}
