package buergerbot

import java.time.LocalDate

data class MockSettings(
    override val date: LocalDate,
    override val formOfAddress: Settings.FormOfAddress?,
    override val name: String,
    override val email: String,
    override val telephoneNumber: String,
    private val enabledCenters: Set<String>
) : Settings {
    override fun isEnabled(center: String): Boolean = center in enabledCenters
}