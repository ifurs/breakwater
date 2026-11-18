package com.astraedus.nudge.ui.overlay

import com.astraedus.nudge.domain.model.BlockMode
import com.astraedus.nudge.domain.model.RuleLabel
import com.astraedus.nudge.testing.en
import com.astraedus.nudge.testing.uk
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Layer **L1**: the block screen's "Rule: ..." line names an in-app feature the way the config
 * screen does, never as the stored key ("SHORTS - Hard Block" was on a real Android 15 screen).
 */
class RuleLabelTextTest {

    private fun label(vararg features: String, mode: BlockMode = BlockMode.HARD_BLOCK) = RuleLabel(
        subject = RuleLabel.Subject.Mode(mode, capsUsage = false),
        features = features.toList()
    ).toUiText()

    @Test
    fun `a stored Shorts key reads as Shorts, not as the key`() {
        assertEquals("Shorts - Hard Block", label("SHORTS").en())
        assertEquals("Shorts - Повне блокування", label("SHORTS").uk())
    }

    @Test
    fun `Reels is a brand name in both languages`() {
        assertEquals("Reels - Hard Block", label("REELS").en())
        assertEquals("Reels - Повне блокування", label("REELS").uk())
        assertEquals("a lower-case key from an older rule reads the same", "Reels - Hard Block", label("reels").en())
    }

    @Test
    fun `Explore and the TikTok feed are translated, from the same resources as the config screen`() {
        assertEquals("Explore - Hard Block", label("EXPLORE").en())
        assertEquals("Цікаве - Повне блокування", label("EXPLORE").uk())
        assertEquals("Feed - Hard Block", label("TIKTOK_FEED").en())
        assertEquals("Стрічка - Повне блокування", label("TIKTOK_FEED").uk())
    }

    @Test
    fun `several features are listed, each named`() {
        assertEquals("Reels, Explore - Hard Block", label("REELS", "EXPLORE").en())
        assertEquals("Reels, Цікаве - Повне блокування", label("REELS", "EXPLORE").uk())
    }

    @Test
    fun `a key nothing knows reads as a capitalised word, never blank or shouting`() {
        assertEquals("Stories - Hard Block", label("STORIES").en())
        assertEquals("Live feed - Hard Block", label("LIVE_FEED").en())
    }
}
