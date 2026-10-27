package com.nexo.studio3d;

import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/** Minimal defensive glTF 2.0 triangle-mesh loader for embedded GLB/data-URI assets. */
final class GltfLoader {
    private static final int MAX_VERTICES=100_000;
    private GltfLoader() {}
    static float[] loadFirstMesh(byte[] file) throws Exception {
        if(file==null||file.length<4)throw new IllegalArgumentException("Asset is empty");
        String jsonText; byte[] binary=null;
        ByteBuffer src=ByteBuffer.wrap(file).order(ByteOrder.LITTLE_ENDIAN);
        if(file.length>=12&&src.getInt(0)==0x46546C67){
            if(src.getInt(4)!=2)throw new IllegalArgumentException("Only GLB 2.0 is supported");
            if(src.getInt(8)!=file.length)throw new IllegalArgumentException("GLB length mismatch");
            int p=12;jsonText=null;
            while(p+8<=file.length){int len=src.getInt(p),type=src.getInt(p+4);p+=8;
                if(len<0||len>file.length-p)throw new IllegalArgumentException("GLB chunk out of bounds");
                if(type==0x4E4F534A)jsonText=new String(file,p,len,StandardCharsets.UTF_8);
                else if(type==0x004E4942){binary=new byte[len];System.arraycopy(file,p,binary,0,len);}
                p+=len;
            }
            if(jsonText==null)throw new IllegalArgumentException("GLB JSON chunk missing");
        }else jsonText=new String(file,StandardCharsets.UTF_8);
        JSONObject root=new JSONObject(jsonText);
        if(!root.has("asset")||!"2.0".equals(root.getJSONObject("asset").optString("version")))throw new IllegalArgumentException("Not glTF 2.0");
        JSONArray buffers=root.optJSONArray("buffers");
        if(binary==null&&buffers!=null&&buffers.length()>0){
            String uri=buffers.getJSONObject(0).optString("uri","");
            if(uri.startsWith("data:")&&uri.contains(";base64,"))binary=Base64.decode(uri.substring(uri.indexOf(";base64,")+8),Base64.DEFAULT);
        }
        if(binary==null)throw new IllegalArgumentException("No embedded buffer found; external .bin buffers are not supported yet");
        JSONArray meshes=root.optJSONArray("meshes");
        if(meshes==null||meshes.length()==0)throw new IllegalArgumentException("No mesh in asset");
        JSONObject primitive=meshes.getJSONObject(0).getJSONArray("primitives").getJSONObject(0);
        if(primitive.optInt("mode",4)!=4)throw new IllegalArgumentException("Only triangle primitives are supported");
        JSONObject attrs=primitive.getJSONObject("attributes");
        int posAccessor=attrs.getInt("POSITION");
        JSONArray accessors=root.getJSONArray("accessors"),views=root.getJSONArray("bufferViews");
        JSONObject pa=accessors.getJSONObject(posAccessor);
        if(pa.optInt("componentType")!=5126||!"VEC3".equals(pa.optString("type")))throw new IllegalArgumentException("POSITION must be float VEC3");
        int vertexCount=pa.getInt("count");
        if(vertexCount<3||vertexCount>MAX_VERTICES)throw new IllegalArgumentException("Unsupported vertex count: "+vertexCount);
        float[] positions=new float[vertexCount*3];
        readPositions(binary,views.getJSONObject(pa.getInt("bufferView")),pa,positions);
        int[] indices;
        if(primitive.has("indices"))indices=readIndices(binary,views,accessors.getJSONObject(primitive.getInt("indices")));
        else {if(vertexCount%3!=0)throw new IllegalArgumentException("Non-indexed triangle vertex count must be divisible by 3");indices=new int[vertexCount];for(int i=0;i<vertexCount;i++)indices[i]=i;}
        if(indices.length<3||indices.length%3!=0||indices.length>MAX_VERTICES*6)throw new IllegalArgumentException("Invalid triangle index count");
        float minX=Float.POSITIVE_INFINITY,minY=minX,minZ=minX,maxX=Float.NEGATIVE_INFINITY,maxY=maxX,maxZ=maxX;
        for(int i=0;i<positions.length;i+=3){float x=positions[i],y=positions[i+1],z=positions[i+2];if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(z))throw new IllegalArgumentException("Non-finite vertex");minX=Math.min(minX,x);minY=Math.min(minY,y);minZ=Math.min(minZ,z);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);maxZ=Math.max(maxZ,z);}
        float cx=(minX+maxX)*.5f,cy=(minY+maxY)*.5f,cz=(minZ+maxZ)*.5f,extent=Math.max(maxX-minX,Math.max(maxY-minY,maxZ-minZ));float scale=extent>1e-6f?2f/extent:1f;
        float[] packed=new float[indices.length*6];
        int out=0;
        for(int index:indices){if(index<0||index>=vertexCount)throw new IllegalArgumentException("Index outside vertex range");int p=index*3;
            packed[out++]=(positions[p]-cx)*scale;packed[out++]=(positions[p+1]-cy)*scale;packed[out++]=(positions[p+2]-cz)*scale;
            packed[out++]=.25f;packed[out++]=.78f;packed[out++]=.95f;
        }
        return packed;
    }
    private static void readPositions(byte[] bin,JSONObject view,JSONObject accessor,float[] out)throws Exception{
        int base=view.optInt("byteOffset",0),viewLength=view.getInt("byteLength"),offset=base+accessor.optInt("byteOffset",0),stride=view.optInt("byteStride",12),count=accessor.getInt("count");
        long end=(long)offset+(long)(count-1)*stride+12;
        if(base<0||viewLength<0||(long)base+viewLength>bin.length||stride<12||offset<base||end>(long)base+viewLength)throw new IllegalArgumentException("POSITION buffer range invalid");
        ByteBuffer b=ByteBuffer.wrap(bin).order(ByteOrder.LITTLE_ENDIAN);
        for(int i=0;i<count;i++){int p=offset+i*stride;out[i*3]=b.getFloat(p);out[i*3+1]=b.getFloat(p+4);out[i*3+2]=b.getFloat(p+8);}
    }
    private static int[] readIndices(byte[] bin,JSONArray views,JSONObject accessor)throws Exception{
        if(!"SCALAR".equals(accessor.optString("type")))throw new IllegalArgumentException("Indices accessor must be SCALAR");
        int component=accessor.getInt("componentType"),count=accessor.getInt("count"),viewId=accessor.getInt("bufferView");
        if(count<3||count>MAX_VERTICES*6)throw new IllegalArgumentException("Index count exceeds limit");
        JSONObject view=views.getJSONObject(viewId);int size=component==5121?1:component==5123?2:component==5125?4:0;
        if(size==0)throw new IllegalArgumentException("Unsupported index component type");
        int base=view.optInt("byteOffset",0),viewLength=view.getInt("byteLength"),offset=base+accessor.optInt("byteOffset",0),stride=view.optInt("byteStride",size);
        long end=(long)offset+(long)(count-1)*stride+size;
        if(base<0||viewLength<0||(long)base+viewLength>bin.length||stride<size||offset<base||end>(long)base+viewLength)throw new IllegalArgumentException("Index buffer range invalid");
        ByteBuffer b=ByteBuffer.wrap(bin).order(ByteOrder.LITTLE_ENDIAN);int[] out=new int[count];
        for(int i=0;i<count;i++){int p=offset+i*stride;out[i]=component==5121?(b.get(p)&255):component==5123?(b.getShort(p)&65535):b.getInt(p);if(out[i]<0)throw new IllegalArgumentException("Negative index");}
        return out;
    }
}
