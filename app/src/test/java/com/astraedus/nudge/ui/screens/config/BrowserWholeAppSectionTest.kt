package com.astraedus.nudge.ui.screens.config

import com.astraedus.nudge.R
import com.astraedus.nudge.domain.model.BlockMode
import com.astraedus.nudge.service.WebDomainDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * L1 (pure unit): the "Default Behavior" switch on App config tells the truth for a browser.
 *
 * By design a DIRECT rule on a browser only blocks websites: the service sends a browser to
 * `evaluateBrowser`, which evaluates the app itself for GROUP rules only (spec follow-up F5). So the
 * editor must not say "Opening Chrome triggers the block below." for a browser. The branch is decided
 * by [WebDomainDetector.isBrowserPackage], the same list the service routes with.
 */
class BrowserWholeAppSectionTest {

    private val chrome = "com.android.chrome"
    private val instagram = "com.instagram.android"

    private fun state(pkg: String, mode: BlockMode = BlockMode.BREATHING) =
        UnifiedAppConfigState(packageName = pkg, appName = pkg, defaultMode = mode)

    @Test
    fun `every browser the service routes is a browser in the editor`() {
        for (pkg in WebDomainDetector.BROWSER_URL_BAR_IDS.keys) {
            assertTrue("$pkg", state(pkg).isBrowser)
            assertTrue("$pkg", WebDomainDetector().isBrowser(pkg))
        }
        assertTrue(state(chrome).isBrowser)
    }

    @Test
    fun `an ordinary app is not a browser`() {
        assertFalse(state(instagram).isBrowser)
        assertFalse(state("").isBrowser)
    }

    @Test
    fun `a browser with a blocking mode does not claim opening it triggers the block`() {
        val s = state(chrome)
        assertTrue(s.blocksWholeApp)
        assertEquals(R.string.config_whole_app_on_browser, s.wholeAppHintRes)
        assertNotEquals(R.string.config_whole_app_on, s.wholeAppHintRes)
        assertEquals(R.string.config_block_whole_app_browser, s.wholeAppTitleRes)
        assertEquals(R.string.config_web_same_mode_browser, s.webSameModeRes)
    }

    @Test
    fun `a browser with the mode off says it opens normally and websites keep their mode`() {
        val s = state(chrome, BlockMode.NONE)
        assertFalse(s.blocksWholeApp)
        assertEquals(R.string.config_whole_app_off_browser, s.wholeAppHintRes)
        assertEquals(R.string.config_block_whole_app_browser, s.wholeAppTitleRes)
    }

    @Test
    fun `an ordinary app keeps the whole app wording`() {
        val on = state(instagram)
        assertEquals(R.string.config_block_whole_app, on.wholeAppTitleRes)
        assertEquals(R.string.config_whole_app_on, on.wholeAppHintRes)
        assertEquals(R.string.config_web_same_mode, on.webSameModeRes)

        val off = state(instagram, BlockMode.NONE)
        assertEquals(R.string.config_whole_app_off, off.wholeAppHintRes)
        val offFeatures = off.copy(
            availableFeatures = listOf(FeatureInfo("REELS", "Reels"))
        )
        assertEquals(R.string.config_whole_app_off_features, offFeatures.wholeAppHintRes)
    }
}
