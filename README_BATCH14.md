# Batch 14 — Real first-pass rendering

Studio AI now uses AndroidX Media3 Transformer 1.11.0 to turn the saved editable timeline into a real MP4 file on-device.

Implemented in this batch:
- source clipping using each timeline clip's start/end bounds
- exclusion of disabled clips
- timeline reorder through the existing saved order
- per-clip 0.5x / 1x / 1.5x / 2x speed using Media3 `EditedMediaItem.Builder#setSpeed`
- sequential audio+video composition
- H.264 video + AAC audio output
- original source is never overwritten
- real completion/error callback shown in the timeline UI

Not claimed in Batch 14:
- Batch 12 catalog effects/transitions are not yet rendered onto pixels
- crossfades are not implemented
- user-facing share/save-to-gallery flow is reserved for Batch 15

The render output is stored in the app-specific external `renders` directory until Batch 15 adds final export/share UX.
