package com.gesturecomm.gestures

/**
 * The 6 gestures the app recognises, each mapped to a phrase.
 *
 * Physical gesture guide (print this for your wrist card at the demo):
 *
 *  FLICK_UP    — snap wrist sharply upward          → "Hi! My name is ___."
 *  FLICK_DOWN  — snap wrist sharply downward        → "Can you help me with this?"
 *  TWIST_CW    — rotate forearm clockwise ~90°      → "How are you?"
 *  TWIST_CCW   — rotate forearm counter-clockwise   → "Thank you so much!"
 *  SHAKE       — shake wrist left-right 2–3×        → "Please wait a moment."
 *  DOUBLE_TAP  — tap watch face twice quickly       → "Yes, I understand."
 */
enum class Gesture(val displayName: String, val phrase: String, val emoji: String) {
    FLICK_UP(
        displayName = "Flick Up",
        phrase      = "Hi! My name is Droov!.",
        emoji       = "👋"
    ),
    FLICK_DOWN(
        displayName = "Flick Down",
        phrase      = "Can you help me with this?",
        emoji       = "🙏"
    ),
    TWIST_CW(
        displayName = "Twist Right",
        phrase      = "How are you?",
        emoji       = "😊"
    ),
    TWIST_CCW(
        displayName = "Twist Left",
        phrase      = "Thank you so much!",
        emoji       = "🙌"
    ),
    SHAKE(
        displayName = "Shake",
        phrase      = "Please wait a moment.",
        emoji       = "✋"
    ),
    DOUBLE_TAP(
        displayName = "Double Tap",
        phrase      = "Yes, I understand.",
        emoji       = "👍"
    );
}
