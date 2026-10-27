# NEXO Studio 3D

Native Android 3D editor foundation built with Java Android UI, OpenGL ES 2 viewport and a C++20 scene core (Android NDK/CMake). This is an actively growing editor foundation, not yet a feature-complete Prisma3D replacement.

## Implemented source features

- Native Android Activity and GLSurfaceView; no WebView.
- OpenGL ES viewport with colored cube primitives, editor grid, world-space RGB XYZ axes, drag orbit and two-finger pinch zoom.
- Interactive Move Gizmo mode: dragging in the viewport translates the current displayed selection/model on the X/Y plane.
- Front, top and side camera presets; Place Camera captures the current editor viewpoint and Camera View switches to that camera.
- Directional-light toggle and environment-fill/light-probe toggle affecting viewport shading.
- Dedicated native editor theme resources: color palette, NexoStudioTheme, reusable button style and editor HUD button/panel drawables.
- Wavefront OBJ position/face parsing, polygon fan triangulation, negative-index support, bounds checks, and rendering the imported triangle mesh in the viewport.
- Limited glTF 2.0 importer: first mesh/first primitive, embedded GLB BIN or base64 data URI, float VEC3 positions and optional unsigned indices. Geometry is centered and normalized before rendering.
- Native C++ scene model with stable entity IDs, cube creation/removal, transform data, bounded undo/redo history and JSON scene serialization.
- Save current native scene data to a user-selected JSON file.
- C++20 animation subsystem source: skeleton/joint hierarchy data, inverse bind matrix storage, translation/scale key tracks, quaternion key tracks, vector interpolation, quaternion slerp and basic skeleton/track validation. It is compiled into the Android native library.
- GitHub Actions builds Java and C++ JNI shared library and verifies APK contains the native library before uploading the artifact.

## Important limitations

- OBJ currently imports position geometry only: OBJ material libraries, texture maps, vertex normals, groups and multi-object scene preservation are not implemented.
- glTF currently imports the first primitive only. It does not yet handle external .bin references, multiple meshes/primitives, glTF materials/textures, normals, tangent space, morph targets, full node transforms or glTF skin/animation extraction.
- The animation subsystem is native data/interpolation infrastructure, not a finished timeline UI or skinned-mesh deformation pipeline. Joint matrices are not yet connected to imported glTF skins or the viewport.
- Camera placement/view is a functional editor camera pose, but cameras are not yet persistent scene entities with editable focal length, clipping planes and animated transforms.
- Lighting toggles adjust the viewport shader's directional/ambient response. Physically based materials, HDR image-based lighting, shadow maps, light-probe capture/baking and offline render output are not implemented yet.
- Move Gizmo currently provides a drag-based translation interaction; axis-handle picking, rotate/scale gizmos, robust object picking, vertex/edge/face editing, extrude, UV tools and production-grade undo of all viewport transforms remain future work.

## Build and artifact

GitHub Actions installs JDK 17, Android platform 35, build tools 35.0.0, NDK 27.2.12479018 and CMake 3.22.1, then runs gradle assembleDebug. A green workflow and a non-empty APK containing libnexo_native.so are required before calling a build verified.

Open GitHub Actions at https://github.com/milydily71-cell/NEXO3D/actions, select a successful Android APK run, then download the NEXO-Studio-3D-debug-apk artifact.

## Next roadmap

1. glTF scene graph, external buffer resolution, all primitives, normals, materials and texture rendering.
2. Camera/light entities with persistent transforms and proper scene serialization.
3. Real axis-handle picking and independent translate/rotate/scale gizmos with selection raycasts.
4. glTF skin/joint and animation-channel parsing; joint matrix generation; GPU skinning and playback UI.
5. Timeline panel, keyframe editing, playhead, loop playback and animation export.
6. Metallic-roughness PBR, image-based lighting, shadow maps and quality presets.
7. Mesh topology editing, UV tools, project load/autosave, asset browser, diagnostics and device smoke tests.
