package buergerbot

import java.time.LocalDate
import java.util.prefs.Preferences

interface Settings {
    val date: LocalDate

    val formOfAddress: FormOfAddress?
    val name: String
    val email: String
    val telephoneNumber: String

    fun isEnabled(center: String): Boolean

    fun save(prefs: Preferences) {
        prefs.put("date", date.toString())
        prefs.put("formOfAddress", formOfAddress.toString())
        prefs.put("name", name)
        prefs.put("email", email)
        prefs.put("telephoneNumber", telephoneNumber)
        prefs.put("centers", centers.map { isEnabled(it) }.joinToString("") { if (it) "1" else "0" })
    }

    enum class FormOfAddress {
        Frau, Herr, Familie, Firma;
    }
}