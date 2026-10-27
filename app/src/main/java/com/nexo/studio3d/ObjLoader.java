package com.nexo.studio3d;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Defensive OBJ v/f triangle loader. Supports polygon fan triangulation and negative vertex indices. */
final class ObjLoader {
    private static final int MAX_VERTICES=100_000;
    private ObjLoader() {}
    static float[] load(byte[] bytes) throws Exception {
        if(bytes==null||bytes.length==0)throw new IllegalArgumentException("Empty OBJ file");
        String text=new String(bytes,StandardCharsets.UTF_8);
        if(text.length()>32*1024*1024)throw new IllegalArgumentException("OBJ exceeds 32 MiB");
        List<float[]> vertices=new ArrayList<>();
        List<Integer> faces=new ArrayList<>();
        for(String raw:text.split("\\R")){
            String line=raw.trim();if(line.isEmpty()||line.startsWith("#"))continue;
            String[] p=line.split("\\s+");
            if(p[0].equals("v")){
                if(p.length<4)continue;
                float x=Float.parseFloat(p[1]),y=Float.parseFloat(p[2]),z=Float.parseFloat(p[3]);
                if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(z))throw new IllegalArgumentException("OBJ contains non-finite vertex");
                vertices.add(new float[]{x,y,z});
                if(vertices.size()>MAX_VERTICES)throw new IllegalArgumentException("OBJ vertex limit exceeded");
            } else if(p[0].equals("f")){
                if(p.length<4)continue;
                int[] polygon=new int[p.length-1];
                for(int i=1;i<p.length;i++){
                    String part=p[i].split("/",-1)[0];if(part.isEmpty())throw new IllegalArgumentException("Face has no position index");
                    int idx=Integer.parseInt(part);idx=idx<0?vertices.size()+idx:idx-1;
                    if(idx<0||idx>=vertices.size())throw new IllegalArgumentException("OBJ face index out of bounds");
                    polygon[i-1]=idx;
                }
                for(int i=1;i<polygon.length-1;i++){faces.add(polygon[0]);faces.add(polygon[i]);faces.add(polygon[i+1]);}
                if(faces.size()>MAX_VERTICES*6)throw new IllegalArgumentException("OBJ triangle limit exceeded");
            }
        }
        if(vertices.isEmpty()||faces.isEmpty())throw new IllegalArgumentException("OBJ contains no renderable v/f geometry");
        float minX=Float.POSITIVE_INFINITY,minY=minX,minZ=minX,maxX=Float.NEGATIVE_INFINITY,maxY=maxX,maxZ=maxX;
        for(float[] v:vertices){minX=Math.min(minX,v[0]);minY=Math.min(minY,v[1]);minZ=Math.min(minZ,v[2]);maxX=Math.max(maxX,v[0]);maxY=Math.max(maxY,v[1]);maxZ=Math.max(maxZ,v[2]);}
        float extent=Math.max(maxX-minX,Math.max(maxY-minY,maxZ-minZ));float scale=extent>1e-6f?2f/extent:1f;
        float cx=(minX+maxX)*.5f,cy=(minY+maxY)*.5f,cz=(minZ+maxZ)*.5f;
        float[] out=new float[faces.size()*6];int k=0;
        for(int idx:faces){float[] v=vertices.get(idx);out[k++]=(v[0]-cx)*scale;out[k++]=(v[1]-cy)*scale;out[k++]=(v[2]-cz)*scale;out[k++]=.35f;out[k++]=.78f;out[k++]=.95f;}
        return out;
    }
}
