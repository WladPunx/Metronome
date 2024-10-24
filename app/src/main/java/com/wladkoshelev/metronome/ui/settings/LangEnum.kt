package com.wladkoshelev.metronome.ui.settings

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/** список локалей приложения */
enum class LangEnum(
    /** классическое имя языка на этом же языке */
    val title: String,
    /** строка локали для применения в системе */
    val lang: String
) {
    ENGLISH("English", "en"),
    RUSSIAN("Русский", "ru"),
    FRENCH("Français", "fr"),
    ITALY("Italiano", "it"),
    GERMAN("Deutsch", "de"),
    POLISH("Polski", "pl"),
    SPANISH("Español", "es");

    companion object {

        /** ШаредПреф для хранения выбранной пользователем локали */
        private fun Context.getLangSettings() = applicationContext.getSharedPreferences("LangSettings", Context.MODE_PRIVATE)
        private const val LANG = "LANG"

        /** метод для применения локали */
        private fun Context.applyLang(lang: String) {
            val locale = Locale(lang)
            Locale.setDefault(locale)
            val configuration = Configuration()
            configuration.locale = locale
            this.resources.updateConfiguration(configuration, null)
            applicationContext.resources.updateConfiguration(configuration, null)
        }

        /** сохранение и установка в локали */
        fun Context.setLang(lang: LangEnum) {
            getLangSettings()
                .edit()
                .putString(LANG, lang.lang)
                .apply()
            applyLang(lang.lang)
        }

        /** применение сохраненной ранее локали, если она есть */
        fun Context.getAndApplyLang(): LangEnum? {
            return getLangSettings().getString(LANG, null)?.let { saveLang ->
                LangEnum.values().find {
                    it.lang == saveLang
                }?.let {
                    applyLang(it.lang)
                    it
                }
            }
        }

    }
}