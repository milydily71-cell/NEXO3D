# NEXO Studio 3D

Native Android 3D editor foundation. This repository contains a Java Android UI, an OpenGL ES viewport, and a C++20 scene core compiled with Android NDK/CMake. It is a growing foundation, not yet a feature-complete Prisma3D replacement.

## Implemented source features

- Native Android Activity and GLSurfaceView; no WebView.
- OpenGL ES viewport with colored cube primitives and a toggleable editor grid.
- Drag orbit camera and two-finger pinch zoom.
- C++20 scene model with entity IDs, cube creation/removal, transform data, bounded undo/redo history, position-key data/interpolation structures, and JSON scene serialization.
- Save current scene data to a user-selected JSON file.
- Defensive GLB/glTF 2.0 triangle mesh import for the first primitive when vertex/index buffers are embedded in GLB or a base64 data URI. The imported triangle mesh is normalized to fit the viewport and rendered with a basic flat color.
- Input size, vertex count, accessor range and index range bounds to reduce malformed-asset and memory-exhaustion risks.
- OpenGL shader compile/link diagnostics and Android lifecycle handling.
- GitHub Actions builds Java and the C++ JNI shared library, verifies a non-empty APK, verifies that the APK contains libnexo_native.so, and uploads the installable debug APK.

## Current limitations

The current glTF importer handles only the first mesh primitive and embedded buffers. It does not yet load external .bin files, multiple primitives, texture images, material properties, normals, morph targets, skeletons or animation clips. Mesh editing (vertices/edges/faces), extrude/UV tools, transform gizmos, a user-facing animation timeline/playback/export, full skeleton authoring and skin deformation, PBR/IBL, shadows, offline rendering, FBX and project reload are not implemented yet. The native scene contains animation keyframe/interpolation foundations, but they are not yet wired into a complete editor UI.

## Build

GitHub Actions installs JDK 17, Android platform 35, build tools 35.0.0, NDK 27.2.12479018 and CMake 3.22.1, then runs Gradle assembleDebug and verifies the APK includes the native library.

APK path: app/build/outputs/apk/debug/app-debug.apk. On GitHub, open Actions > Android APK > a successful run > Artifacts.

## Roadmap

1. glTF external buffers, multiple primitives, normals, textures and metallic-roughness materials.
2. Selection, transform gizmos and editable vertices/edges/faces.
3. Exposed timeline UI, transform editing, keyframe creation/playback and animation export.
4. Skeleton hierarchy, skin weights, joint matrices and skinned animation playback.
5. PBR metallic-roughness, image-based lighting, shadows and render presets.
6. Scene/project load, resource management, autosave, diagnostics and device smoke tests.
