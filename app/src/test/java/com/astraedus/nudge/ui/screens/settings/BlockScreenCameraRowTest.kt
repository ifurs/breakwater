package com.astraedus.nudge.ui.screens.settings

import com.astraedus.nudge.R
import com.astraedus.nudge.testing.enString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockScreenCameraRowTest {

    @Test
    fun `turning on without the camera asks for it`() {
        assertTrue(shouldRequestCameraOnToggle(enable = true, granted = false))
    }

    @Test
    fun `turning on with the camera already granted does not ask`() {
        assertFalse(shouldRequestCameraOnToggle(enable = true, granted = true))
    }

    @Test
    fun `turning off never asks`() {
        assertFalse(shouldRequestCameraOnToggle(enable = false, granted = false))
        assertFalse(shouldRequestCameraOnToggle(enable = false, granted = true))
    }

    @Test
    fun `the note shows only while on and not granted`() {
        assertTrue(blockScreenCameraNoteVisible(enabled = true, granted = false))
        assertFalse(blockScreenCameraNoteVisible(enabled = true, granted = true))
        assertFalse(blockScreenCameraNoteVisible(enabled = false, granted = false))
        assertFalse(blockScreenCameraNoteVisible(enabled = false, granted = true))
    }

    @Test
    fun `copy is the plain wording`() {
        assertEquals("Show the camera on block screens", enString(R.string.settings_camera_title))
        assertEquals(
            "You see yourself instead of a plain screen. Nothing is recorded.",
            enString(R.string.settings_camera_subtitle)
        )
        assertEquals("Camera is off, so block screens stay plain.", enString(R.string.settings_camera_off_note))
    }
}
