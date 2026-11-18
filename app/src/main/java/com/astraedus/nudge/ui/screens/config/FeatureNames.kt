package com.astraedus.nudge.ui.screens.config

import com.astraedus.nudge.R
import com.astraedus.nudge.domain.text.UiText

/**
 * The one mapping from a stored in-app feature key ("REELS", "SHORTS", "EXPLORE", "TIKTOK_FEED")
 * to the name the user reads: the config screen's rows and headline, and the block screen's
 * "Rule: ..." line.
 *
 * Reels and Shorts are brand names and stay as they are ([UnifiedAppConfigState.FEATURES_BY_PACKAGE]
 * holds them); Explore and the TikTok feed are ordinary words and come from resources. A key nothing
 * here knows (a rule imported from a newer version) reads as the key, capitalised, never blank.
 */
object FeatureNames {

    fun of(key: String): UiText {
        val normalised = key.trim().uppercase()
        return when (normalised) {
            "EXPLORE" -> UiText.of(R.string.config_feature_explore)
            "TIKTOK_FEED" -> UiText.of(R.string.config_feature_tiktok_feed)
            else -> UiText.Raw(brandName(normalised) ?: fallback(key.trim()))
        }
    }

    private fun brandName(key: String): String? = UnifiedAppConfigState.FEATURES_BY_PACKAGE.values
        .asSequence()
        .flatten()
        .firstOrNull { it.key == key }
        ?.displayName

    private fun fallback(key: String): String =
        key.lowercase().replace('_', ' ').replaceFirstChar { it.uppercaseChar() }
}
