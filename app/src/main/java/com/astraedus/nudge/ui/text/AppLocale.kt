package com.astraedus.nudge.ui.text

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * The locales a context should be resolved with, given the user's per-app language choice
 * ([appLocales], Android 13+ "App languages") — or null when the context already has the right
 * ones. The one decision behind [withAppLocale], kept free of Android types so a JVM test pins it:
 * an app-language choice wins, no choice means "follow the system" (null, the context's own).
 */
internal fun chooseAppLocales(appLocales: List<Locale>): List<Locale>? =
    appLocales.takeIf { it.isNotEmpty() }

/**
 * This context, with the user's per-app language applied.
 *
 * Android applies a per-app language (`cmd locale set-app-locales`, Settings > App languages) to
 * ACTIVITY contexts only. Text resolved through the Application, a Service, a receiver, a worker or
 * a widget comes out in the SYSTEM language: the ongoing notification, the Home prompts built from
 * the application context, the block screen's rule line. Every place that turns a string resource
 * into user-visible text outside an Activity resolves through this (see `docs/i18n.md`).
 *
 * Below API 33 there is no per-app language, and with no choice made the context is returned as is.
 */
fun Context.withAppLocale(): Context {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return this
    val chosen = try {
        val tags = getSystemService(LocaleManager::class.java)?.applicationLocales ?: return this
        chooseAppLocales((0 until tags.size()).mapNotNull { tags.get(it) })
    } catch (_: RuntimeException) {
        null
    } ?: return this
    val config = Configuration(resources.configuration).apply {
        setLocales(LocaleList(*chosen.toTypedArray()))
    }
    return createConfigurationContext(config)
}

/** The language tags [withAppLocale] resolves with, e.g. "uk-UA"; changes when the language does. */
fun Context.appLocaleTag(): String =
    withAppLocale().resources.configuration.locales.toLanguageTags()
