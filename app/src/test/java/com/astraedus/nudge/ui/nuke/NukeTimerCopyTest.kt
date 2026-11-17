package com.astraedus.nudge.ui.nuke

import com.astraedus.nudge.domain.nuke.NukeTimer
import com.astraedus.nudge.testing.en
import com.astraedus.nudge.testing.uk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/** L1: every line the Nuke timer (#184) shows. */
class NukeTimerCopyTest {

    private val minute = 60_000L

    @Test
    fun `time left rounds up, so it never says 0 while time is left`() {
        assertEquals("1 min", NukeTimerCopy.timeLeft(1).en())
        assertEquals("1 min", NukeTimerCopy.timeLeft(minute).en())
        assertEquals("2 min", NukeTimerCopy.timeLeft(minute + 1).en())
        assertEquals("24 min", NukeTimerCopy.timeLeft(23 * minute + 30_000).en())
        assertEquals("1 h", NukeTimerCopy.timeLeft(60 * minute).en())
        assertEquals("1 h 5 min", NukeTimerCopy.timeLeft(65 * minute).en())
        assertEquals("2 h", NukeTimerCopy.timeLeft(120 * minute).en())
    }

    @Test
    fun `the refusal says the code is locked, for how long, and what to do`() {
        assertEquals(
            "Your code is locked for 15 more min. Scan again after that.",
            NukeTimerCopy.refusal(15 * minute).en()
        )
        assertEquals("Your code is locked for 1 more hour. Scan again after that.", NukeTimerCopy.refusal(60 * minute).en())
        assertEquals("Your code is locked for 2 more hours. Scan again after that.", NukeTimerCopy.refusal(120 * minute).en())
        assertEquals("Your code is locked for 1 h 5 min more. Scan again after that.", NukeTimerCopy.refusal(65 * minute).en())
        assertEquals("rounds up", "Your code is locked for 1 more min. Scan again after that.", NukeTimerCopy.refusal(1).en())
    }

    @Test
    fun `the status line counts down while the code is locked, and is gone once it runs out`() {
        assertNull(NukeTimerCopy.statusLine(null))
        assertEquals("Code locked \u00b7 15 min left", NukeTimerCopy.statusLine(15 * minute)!!.en())
        assertEquals("Code locked \u00b7 1 h 5 min left", NukeTimerCopy.statusLine(65 * minute)!!.en())
        // Round 1: no "Timer done". A run-out lock leaves the plain ON card.
        assertNull(NukeTimerCopy.statusLine(0))
        assertNull(NukeTimerCopy.statusLine(-1))
    }

    @Test
    fun `no line calls it a timer, since it locks the code and never ends Nuke`() {
        val lines = listOf(
            NukeTimerCopy.PICKER_LABEL,
            NukeTimerCopy.refusal(15 * minute),
            NukeTimerCopy.statusLine(15 * minute)!!
        ) + NukeTimer.CHOICES_MINUTES.map(NukeTimerCopy::choiceLabel)
        lines.forEach { line -> assertFalse(line.en(), line.en().contains("timer", ignoreCase = true)) }
        assertEquals("Lock code for", NukeTimerCopy.PICKER_LABEL.en())
    }

    @Test
    fun `every picker choice has its own plain label`() {
        assertEquals(
            listOf("None", "15 min", "30 min", "45 min", "1 hour", "2 hours"),
            NukeTimer.CHOICES_MINUTES.map { NukeTimerCopy.choiceLabel(it).en() }
        )
    }

    @Test
    fun `Ukrainian counts hours in the right plural form and keeps the numbers`() {
        assertEquals("Код заблоковано ще на 15 хв. Скануйте ще раз, коли цей час мине.", NukeTimerCopy.refusal(15 * minute).uk())
        assertEquals("Код заблоковано ще на 1 годину. Скануйте ще раз, коли цей час мине.", NukeTimerCopy.refusal(60 * minute).uk())
        assertEquals("Код заблоковано ще на 2 години. Скануйте ще раз, коли цей час мине.", NukeTimerCopy.refusal(120 * minute).uk())
        assertEquals("Код заблоковано ще на 1 год 5 хв. Скануйте ще раз, коли цей час мине.", NukeTimerCopy.refusal(65 * minute).uk())
        assertEquals("Код заблоковано · ще 1 год 5 хв", NukeTimerCopy.statusLine(65 * minute)!!.uk())
        assertEquals(
            listOf("Без блокування", "15 хв", "30 хв", "45 хв", "1 година", "2 години"),
            NukeTimer.CHOICES_MINUTES.map { NukeTimerCopy.choiceLabel(it).uk() }
        )
    }
}
