package com.nexo.studio3d;
final class NativeScene {
    static { System.loadLibrary("nexo_native"); }
    private NativeScene() {}
    static native void nativeReset();
    static native int nativeAddCube();
    static native boolean nativeUndo();
    static native boolean nativeRedo();
    static native boolean nativeRemoveLast();
    static native int nativeCount();
    static native int nativeSelectedId();
    static native String nativeSerialize();
    static native String nativeInspectGltf(byte[] bytes);
    static int count() { return nativeCount(); }
}
