# NEXO Studio 3D

Native Android 3D-editor project. The current milestone is a small, installable Android application with a live OpenGL ES viewport and touch orbit controls.

## Current implementation

- Native Android Activity and GLSurfaceView (no WebView).
- OpenGL ES 2.0 rendering of colored 3D cube primitives.
- Drag-to-orbit camera controls.
- Add Cube button for adding primitive instances.
- Android document picker for selecting .glb / .gltf files.
- GitHub Actions build workflow that assembles a debug APK and uploads it as an artifact.

## Not implemented yet

Selecting a GLB/glTF file is not the same as importing and rendering it. The glTF mesh/material/texture/skin/animation loader is still pending. Mesh editing, vertex/edge/face operations, skeleton creation and skinning, keyframe animation, project serialization, PBR, shadows, and offline rendering are also not implemented in this first milestone. The Model, Rig, Animate and Render buttons currently explain their planned status instead of pretending these features work.

## Build

The GitHub Actions workflow installs Android SDK platform 35 and uses JDK 17 with Gradle 8.9:

    gradle --no-daemon assembleDebug

APK output: app/build/outputs/apk/debug/app-debug.apk.

On GitHub, open Actions > Android APK > latest successful run > Artifacts to download the debug APK. A successful CI run is required before considering the artifact verified.

## Roadmap

1. Add a visible editor grid, selection and transform gizmos.
2. Add editable primitive meshes and undo/redo.
3. Integrate a real glTF 2.0 loader for GLB/glTF meshes, PBR materials and textures.
4. Add skeleton hierarchy, skinning and imported animation playback.
5. Add editable keyframes, timeline tracks and animation export.
6. Add project save/load, render settings and image export.
