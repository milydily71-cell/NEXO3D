# Device and instrumentation test plan

Run the APK on an API 24+ device and verify launch/resume after backgrounding, drag orbit, two-finger pinch zoom, add/remove cube, undo/redo, grid toggle, and selection of a .glb file followed by a bounded file read and glTF validation message. Collect native crashes with adb logcat and attach them to a failing run.

A CI compile is not a substitute for a real-device smoke test.
