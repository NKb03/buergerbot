package buergerbot

import buergerbot.Settings.FormOfAddress
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.util.StringConverter
import javafx.util.converter.DateStringConverter
import javafx.util.converter.LocalDateStringConverter
import java.text.DateFormat
import java.time.LocalDate
import java.time.chrono.Chronology
import java.time.format.FormatStyle
import java.util.*
import java.util.prefs.Preferences

class SettingsView : Settings, VBox(5.0) {
    private val datePicker = DatePicker(LocalDate.now())
    private val formOfAddressBox = ChoiceBox<FormOfAddress>()
    private val nameField = TextField()
    private val emailField = TextField()
    private val numberField = TextField()
    private val centerToggles = centers.associateWith { center -> RadioButton(center) }

    init {
        datePicker.converter = LocalDateStringConverter(FormatStyle.MEDIUM, Locale.GERMANY, null)
        datePicker.setDayCellFactory {
            object : DateCell() {
                override fun updateItem(item: LocalDate?, empty: Boolean) {
                    super.updateItem(item, empty)
                    isDisable = empty || item == null || item < LocalDate.now()
                }
            }
        }
        formOfAddressBox.items.setAll(*FormOfAddress.values())
        nameField.promptText = "Name"
        emailField.promptText = "E-Mail"
        numberField.promptText = "Telefon"
        children.addAll(
            section("Datum"),
            datePicker,
            section("Persönliche Daten"),
            HBox(Label("Anrede: ").also { it.font = Font(15.0) }, formOfAddressBox),
            nameField,
            emailField,
            numberField,
            section("Mögliche Kundenzentren")
        )
        children.addAll(centerToggles.values)
    }

    private fun section(title: String): Label = Label(title).apply {
        font = Font.font("Regular", FontWeight.BOLD, 18.0)
    }

    override val date: LocalDate
        get() = datePicker.value
    override val formOfAddress: FormOfAddress?
        get() = formOfAddressBox.value
    override val name: String
        get() = nameField.text
    override val email: String
        get() = emailField.text
    override val telephoneNumber: String
        get() = numberField.text

    override fun isEnabled(center: String): Boolean = centerToggles[center]?.isSelected == true

    fun load(prefs: Preferences) {
        datePicker.value = LocalDate.parse(prefs.get("date", LocalDate.now().toString()))
        formOfAddressBox.value = prefs.get("formOfAddress", null)?.let(FormOfAddress::valueOf)
        nameField.text = prefs.get("name", "")
        emailField.text = prefs.get("email", "")
        numberField.text = prefs.get("telephoneNumber", "")
        val centerEnabled = prefs.get("centers", "1".repeat(centers.size))
        for ((i, center) in centers.withIndex()) {
            centerToggles.getValue(center).isSelected = centerEnabled[i] == '1'
        }
    }
}