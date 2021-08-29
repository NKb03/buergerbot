package buergerbot

import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight

class AppointmentTable(sites: List<Site>) : HBox() {
    private val lists = sites.associateWith { ListView<Appointment>() }
    private val columns = lists.map { (site, list) ->
        val header = Label(site.name)
        header.font = Font.font("System", FontWeight.BOLD, 15.0)
        VBox(header, list)
    }
    private var onSelect: (Appointment) -> Unit = { }

    init {
        for (list in lists.values) {
            list.setCellFactory { AppointmentCell() }
        }
        children.addAll(columns)
    }

    fun setOnSelect(handler: (Appointment) -> Unit) {
        onSelect = handler
    }

    fun updateAppointments(site: Site, appointments: List<Appointment>): Boolean {
        val list = lists.getValue(site)
        if (list.items.toList() == appointments) return false
        list.items.setAll(appointments)
        return true
    }

    private inner class AppointmentCell : ListCell<Appointment>() {
        override fun updateItem(item: Appointment?, empty: Boolean) {
            super.updateItem(item, empty)
            graphic = if (item == null || empty) null else Button(item.time)
            graphic?.setOnMouseClicked { onSelect(item!!) }
        }
    }
}