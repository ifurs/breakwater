package com.astraedus.nudge.ui.nuke

import com.astraedus.nudge.R
import com.astraedus.nudge.domain.nuke.NukeTimer
import com.astraedus.nudge.domain.text.UiText

/**
 * Every string the Nuke timer (#184) shows, as pure functions returning [UiText] so the copy has a
 * JVM test and follows the user's language.
 *
 * The words say what the timer DOES: it locks the code, so the code can't end Nuke yet. Nuke itself
 * does not turn off when it runs out ("Timer" alone read that way on the device, round 1).
 */
object NukeTimerCopy {

    /** The picker row's label in the off card. */
    val PICKER_LABEL: UiText = UiText.of(R.string.nuke_timer_picker_label)

    /** "24 min", "1 h 5 min", "2 h". Rounded UP, so it never says "0 min" while time is left. */
    fun timeLeft(ms: Long): UiText {
        val (hours, rest) = hoursAndMinutes(ms)
        return when {
            hours == 0L -> UiText.of(R.string.nuke_timer_left_minutes, rest)
            rest == 0L -> UiText.of(R.string.nuke_timer_left_hours, hours)
            else -> UiText.of(R.string.nuke_timer_left_hours_minutes, hours, rest)
        }
    }

    /** What a correct scan gets while the code is locked. */
    fun refusal(waitMs: Long): UiText {
        val (hours, rest) = hoursAndMinutes(waitMs)
        return when {
            hours == 0L -> UiText.of(R.string.nuke_timer_refusal_minutes, rest)
            rest == 0L -> UiText.plural(R.plurals.nuke_timer_refusal_hours, hours.toInt())
            else -> UiText.of(R.string.nuke_timer_refusal_hours_minutes, hours, rest)
        }
    }

    /**
     * The status card's line under "Nuke is on" while the code is locked. Null with no timer AND once
     * it has run out: from then on the card is the plain ON card.
     */
    fun statusLine(leftMs: Long?): UiText? =
        if (leftMs == null || leftMs <= 0) null else UiText.of(R.string.nuke_timer_status, timeLeft(leftMs))

    /** The picker's label for a choice in [NukeTimer.CHOICES_MINUTES]. */
    fun choiceLabel(minutes: Int): UiText = when {
        minutes <= 0 -> UiText.of(R.string.nuke_timer_choice_none)
        minutes % 60 == 0 -> UiText.plural(R.plurals.nuke_timer_choice_hours, minutes / 60)
        else -> UiText.of(R.string.nuke_timer_choice_minutes, minutes)
    }

    private fun hoursAndMinutes(ms: Long): Pair<Long, Long> {
        val minutes = ((ms.coerceAtLeast(0) + 59_999) / 60_000).coerceAtLeast(1)
        return minutes / 60 to minutes % 60
    }
}
