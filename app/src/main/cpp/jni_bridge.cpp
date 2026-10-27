#include <jni.h>
#include <string>
#include <vector>
#include "SceneCore.h"
#include "GltfAssetReader.h"
namespace { nexo::SceneCore scene; }
extern "C" JNIEXPORT void JNICALL Java_com_nexo_studio3d_NativeScene_nativeReset(JNIEnv*,jclass){scene=nexo::SceneCore();}
extern "C" JNIEXPORT jint JNICALL Java_com_nexo_studio3d_NativeScene_nativeAddCube(JNIEnv*,jclass){return static_cast<jint>(scene.addCube());}
extern "C" JNIEXPORT jboolean JNICALL Java_com_nexo_studio3d_NativeScene_nativeUndo(JNIEnv*,jclass){return scene.undo()?JNI_TRUE:JNI_FALSE;}
extern "C" JNIEXPORT jboolean JNICALL Java_com_nexo_studio3d_NativeScene_nativeRedo(JNIEnv*,jclass){return scene.redo()?JNI_TRUE:JNI_FALSE;}
extern "C" JNIEXPORT jboolean JNICALL Java_com_nexo_studio3d_NativeScene_nativeRemoveLast(JNIEnv*,jclass){return scene.removeLast()?JNI_TRUE:JNI_FALSE;}
extern "C" JNIEXPORT jint JNICALL Java_com_nexo_studio3d_NativeScene_nativeCount(JNIEnv*,jclass){return scene.count();}
extern "C" JNIEXPORT jint JNICALL Java_com_nexo_studio3d_NativeScene_nativeSelectedId(JNIEnv*,jclass){return static_cast<jint>(scene.selectedId());}
extern "C" JNIEXPORT jstring JNICALL Java_com_nexo_studio3d_NativeScene_nativeSerialize(JNIEnv*env,jclass){auto s=scene.serialize();return env->NewStringUTF(s.c_str());}
extern "C" JNIEXPORT jstring JNICALL Java_com_nexo_studio3d_NativeScene_nativeInspectGltf(JNIEnv*env,jclass,jbyteArray bytes){if(!bytes)return env->NewStringUTF("No file selected");jsize n=env->GetArrayLength(bytes);std::vector<std::uint8_t>d(static_cast<std::size_t>(n));if(n>0)env->GetByteArrayRegion(bytes,0,n,reinterpret_cast<jbyte*>(d.data()));auto r=nexo::inspectGltf(d.data(),d.size());auto msg=(r.valid?std::string("VALID: "):std::string("INVALID: "))+r.message;return env->NewStringUTF(msg.c_str());}
