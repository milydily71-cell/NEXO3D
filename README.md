# NEXO Studio 3D

Native Android 3D editor foundation. This repository now contains Java Android UI code, an OpenGL ES viewport, and a C++20 scene core compiled through Android NDK/CMake. It is a working foundation, not yet a feature-complete Prisma3D replacement.

## Implemented source features

- Native Android Activity and GLSurfaceView; no WebView.
- OpenGL ES viewport with colored cube primitives, visible toggleable editor grid, drag orbit, and two-finger pinch zoom.
- Native C++ scene model with stable entity IDs, cube creation/removal, transform data, bounded undo/redo history, and JSON scene serialization.
- Save current scene data to a user-selected JSON file.
- Native GLB/glTF 2.0 container inspection with GLB header/chunk bounds validation and a 32 MiB input cap.
- OpenGL shader compile/link diagnostics and context recreation path.
- GitHub Actions workflow builds Java plus the C++ JNI shared library and verifies the APK contains the native library before uploading it.

## Important limitations (not falsely marked complete)

The glTF reader currently validates container/JSON metadata only. It does **not** decode accessors/buffers into renderable meshes or load textures/materials. The viewport still renders cube primitives. Vertex/edge/face editing, extrude/UV tools, transform gizmos, complete skeleton authoring and skin-weight deformation, animation timeline UI/playback/export, PBR/IBL, shadows, offline rendering, and FBX are not implemented yet. Native keyframe data/interpolation structures exist in the C++ scene core, but are not yet exposed as a finished animation editor.

## Build

GitHub Actions installs JDK 17, Android platform 35, build tools 35.0.0, NDK 27.2.12479018 and CMake 3.22.1, then runs Gradle assembleDebug. A green workflow and a non-empty APK with libnexo_native.so are required before calling a build verified.

APK path: app/build/outputs/apk/debug/app-debug.apk. On GitHub, open Actions > Android APK > a successful run > Artifacts.

## Roadmap

1. Real glTF 2.0 buffer/accessor/mesh decoding, texture/material loading and imported mesh rendering.
2. Selection, transform gizmos and editable vertices/edges/faces.
3. Exposed timeline UI, transform editing, keyframe creation/playback and animation export.
4. Skeleton hierarchy, skin weights, joint matrices and skinned animation playback.
5. PBR metallic-roughness, image-based lighting, shadows and render presets.
6. Scene/project load, resource management, autosave, diagnostics, and device smoke tests.
