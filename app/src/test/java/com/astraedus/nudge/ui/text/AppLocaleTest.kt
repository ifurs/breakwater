package com.astraedus.nudge.ui.text

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Layer **L1, pure JVM unit**: the decision behind `Context.withAppLocale()` — which locales win.
 * The Android half (reading `LocaleManager`, building the configuration context) needs a device:
 * `cmd locale set-app-locales dev.ifurs.breakwater --locales uk-UA`, then the ongoing notification,
 * the Home prompts and the block screen's rule line must read Ukrainian.
 */
class AppLocaleTest {

    @Test
    fun `a per-app language choice wins, in the order the user gave it`() {
        val chosen = listOf(Locale.forLanguageTag("uk-UA"), Locale.forLanguageTag("en-GB"))

        assertEquals(chosen, chooseAppLocales(chosen))
    }

    @Test
    fun `no choice follows the system, so the context is left as it is`() {
        assertNull(chooseAppLocales(emptyList()))
    }
}
