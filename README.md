# GestureComm — Pixel Watch 2 Gesture-to-Phrase App

A Wear OS app that classifies 6 real wrist gestures and speaks the mapped
phrase aloud through the paired phone's TTS engine. Built for live demo.

---

## Project structure

```
GestureComm/
└── wear/          ← Wear OS app (sideloaded to Pixel Watch 2)
    └── src/main/kotlin/com/gesturecomm/
        ├── gestures/
        │   ├── GestureDefinitions.kt   ← phrases live here — easy to edit
        │   ├── GestureClassifier.kt    ← threshold logic, tunable constants
        │   └── GestureService.kt       ← foreground service, sensor loop
        │
        └── ui/
            └── MainActivity.kt         ← Compose UI
```

---

## Quick-start (30 minutes to demo-ready)

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Pixel Watch 2 with Developer Mode enabled
- Wear OS debugging enabled on the watch

### 1 — Enable Developer Mode on Pixel Watch 2
1. Settings → System → About → tap **Build Number** 7 times
2. Settings → Developer Options → ADB Debugging → ON
3. Settings → Developer Options → Debug over Wi-Fi → ON
   (note the IP address shown, e.g. `192.168.1.42:5555`)

### 2 — Build and install

Open the project in Android Studio. Two run configurations:

**Watch app:**
```
adb connect 192.168.1.42:5555          # use your watch's IP
adb -s 192.168.1.42:5555 install wear/build/outputs/apk/debug/wear-debug.apk
```
Or just press ▶ in Android Studio with the watch selected as target device.

### 3 — Grant permissions
On the watch, when prompted: allow **Body Sensors** permission.

---

## The 6 gestures

| # | Gesture | How to do it | Phrase spoken |
|---|---------|--------------|---------------|
| 1 | **Flick Up** | Snap wrist sharply upward | *"Hi! My name is ___."* |
| 2 | **Flick Down** | Snap wrist sharply downward | *"Can you help me with this?"* |
| 3 | **Twist Right** | Rotate forearm clockwise ~90° | *"How are you?"* |
| 4 | **Twist Left** | Rotate forearm counter-clockwise ~90° | *"Thank you so much!"* |
| 5 | **Shake** | Shake wrist left-right 2–3 times | *"Please wait a moment."* |
| 6 | **Double Tap** | Tap the watch face twice quickly | *"Yes, I understand."* |

**Practice tip:** Each gesture needs ~0.5–1 second of committed motion.
Flicks should be sharp snaps, not slow swings. Twists need ~90° of rotation.

---

## Customising phrases

Open `wear/src/main/kotlin/com/gesturecomm/gestures/GestureDefinitions.kt`
and edit the `phrase` field for any gesture:

```kotlin
FLICK_UP(
    displayName = "Flick Up",
    phrase      = "Hi! My name is Alex.",   // ← put the presenter's name here
    emoji       = "👋"
),
```

Rebuild and re-sideload after changing.

---

## Tuning gesture sensitivity

If gestures misfire or don't trigger, open `GestureClassifier.kt`.
Each detector has a clearly labelled threshold constant at the top:

```kotlin
// FLICK_UP — increase if firing accidentally, decrease if not triggering
private fun detectFlickUp(): Gesture? {
    val THRESHOLD = 18f   // ← m/s²
```

**Misfiring too often** → increase the threshold value
**Not triggering** → decrease the threshold value

Typical adjustment range: ±3–5 units.

---

## Architecture overview

```
Pixel Watch 2
─────────────────────────────────
SensorManager (50 Hz)
  │ accel + gyro events
  ▼
GestureClassifier
  │ threshold detection
  ▼
GestureService (foreground)
  ├── Haptic pulse (watch)
  ├── Broadcast → MainActivity UI
  └── Wearable MessageClient ──────────→ Watch Speech
```

No internet required. Entirely local. Works on airplane mode.

---

## Troubleshooting

**Watch not appearing as ADB target**
- Confirm Wi-Fi debugging is enabled on the watch
- Ensure phone and watch are on the same Wi-Fi network
- Try: `adb disconnect` then `adb connect <watch-ip>:5555`

**Gestures not triggering**
- Grant Body Sensors permission on the watch
- Check the foreground notification is visible (means service is running)
- Make gestures more deliberate — slower, larger motions work best during tuning

**Wrong gestures firing**
- Flick Up and Flick Down can sometimes overlap — increase threshold to 22f
- Twist detection relies on gyro-Z; if the watch is worn on right wrist, CW/CCW may be inverted — swap the positive/negative threshold signs in `detectTwistCW` / `detectTwistCCW`

---

## Extending

- **More phrases:** Add entries to the `Gesture` enum (currently capped at 6 gestures per the classifier design; extendable to ~12 with rule-based approach)
- **Sequences:** See architecture notes in parent README — the sequence engine adds multi-gesture combos
- **TFLite model:** Replace `GestureClassifier` with a TFLite `Interpreter` for higher accuracy and more gesture types



---