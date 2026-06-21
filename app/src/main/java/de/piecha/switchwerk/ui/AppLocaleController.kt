package de.piecha.switchwerk.ui

import android.app.LocaleManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import de.piecha.switchwerk.domain.model.AppLanguage

object AppLocaleController {
    fun apply(context: Context, language: AppLanguage): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applyPlatformLocales(context, language)
        } else {
            applyLegacyLocales(context, language)
        }
    }

    private fun applyPlatformLocales(context: Context, language: AppLanguage): Boolean {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        val requestedLocales = language.languageTag
            ?.let(LocaleList::forLanguageTags)
            ?: LocaleList.getEmptyLocaleList()
        if (localeManager.applicationLocales == requestedLocales) return false
        localeManager.applicationLocales = requestedLocales
        return true
    }

    @Suppress("DEPRECATION")
    private fun applyLegacyLocales(context: Context, language: AppLanguage): Boolean {
        val requestedLocales = language.languageTag
            ?.let(LocaleList::forLanguageTags)
            ?: Resources.getSystem().configuration.locales
        if (context.resources.configuration.locales == requestedLocales) return false

        val configuration = context.resources.configuration
        configuration.setLocales(requestedLocales)
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        context.applicationContext.resources.updateConfiguration(
            configuration,
            context.applicationContext.resources.displayMetrics
        )
        return true
    }
}
