package com.projectx.app.navmap.position

/**
 * Platform-agnostic sensor feed consumed by [PdrEngine]. The Android implementation
 * drives this from `SensorManager` listeners (TYPE_GAME_ROTATION_VECTOR + TYPE_STEP_DETECTOR,
 * with TYPE_ACCELEROMETER and TYPE_ROTATION_VECTOR as fallbacks). Unit tests feed it from
 * recorded CSV walks.
 */
interface SensorSource {
    /** Device heading in degrees (0 = device-forward points towards geographic north). */
    val heading: () -> Double

    /** Called by the platform when the step detector fires (or when a peak is detected). */
    fun registerStepListener(onStep: () -> Unit)
    fun unregisterStepListener()
}
