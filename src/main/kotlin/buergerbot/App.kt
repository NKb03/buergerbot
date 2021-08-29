package buergerbot

import com.sun.webkit.dom.HTMLInputElementImpl
import javafx.application.Application
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.KeyCombination
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import javafx.stage.Stage
import org.w3c.dom.Document
import org.w3c.dom.html.HTMLSelectElement
import java.util.*
import java.util.prefs.Preferences
import kotlin.concurrent.fixedRateTimer

class App : Application() {
    private lateinit var browser: WebView
    private lateinit var stage: Stage
    private lateinit var settings: SettingsView
    private lateinit var root: VBox
    private var content: Node
        get() = root.children[1]
        set(node) {
            if (root.children.size == 1) root.children.add(node)
            else root.children[1] = node
            stage.sizeToScene()
        }
    private var timer: Timer? = null

    private val prefs = Preferences.userNodeForPackage(javaClass)

    private val engine get() = browser.engine

    private val document get() = engine.document

    private fun setup(primaryStage: Stage) {
        stage = primaryStage
        browser = WebView()
        settings = SettingsView()
        settings.load(prefs)
        browser.setPrefSize(1040.0, 1050.0)
        val newSearch = MenuItem("Neue Suche")
        newSearch.accelerator = KeyCombination.valueOf("Ctrl+N")
        newSearch.setOnAction { showSettings() }
        root = VBox(MenuBar(Menu("Datei", null, newSearch)))
        stage.scene = Scene(root)
        stage.sizeToScene()
        stage.centerOnScreen()
        stage.icons.add(Image(javaClass.getResourceAsStream("buergerbot.png")))
    }

    private fun showSettings() {
        timer?.cancel()
        timer = null
        val continueBtn = Button("Weiter")
        continueBtn.setOnAction { showBrowser() }
        val layout = VBox(settings, continueBtn)
        content = layout
    }

    private fun showBrowser() {
        engine.onSuccess {
            listenForConfirmation()
        }
        engine.load(ROOT_URL)
        content = browser
    }

    private fun showTable(sites: List<Site>): AppointmentTable {
        val t = AppointmentTable(sites)
        content = t
        t.setOnSelect { appointment ->
            showAppointmentConfirmation(appointment)
        }
        return t
    }

    private fun showAppointmentConfirmation(appointment: Appointment) {
        with(Stage()) {
            val view = WebView()
            view.setPrefSize(1040.0, 800.0)
            view.engine.load(appointment.url)
            scene = Scene(ProgressBar())
            view.engine.onSuccess {
                fillIn(view.engine.document)
                scene.root = view
                sizeToScene()
            }
            show()
        }
    }

    private fun fillIn(doc: Document) {
        val formOfAddress = doc.getElementsByTagName("select").item(0) as HTMLSelectElement? ?: return
        formOfAddress.selectedIndex = settings.formOfAddress?.ordinal ?: 0
        val (_, name, email, telephone, accept) = doc.getElementsByTagName("input")
            .toList().map { it as HTMLInputElementImpl }.takeIf { it.size == 6 } ?: return
        name.value = settings.name
        email.value = settings.email
        telephone.value = settings.telephoneNumber
        accept.checked = true
    }

    private fun listenForConfirmation() {
        val submit = findSubmitButton(document)
        submit.setOnclick {
            engine.onSuccess { d ->
                confirmedSelection(d)
            }
        }
    }

    private fun confirmedSelection(d: Document) {
        val sites = extractSites(d)
        val table = showTable(sites.filter { settings.isEnabled(it.name) })
        startListening(sites, table)
    }

    private fun startListening(sites: List<Site>, t: AppointmentTable) {
        var changed: Boolean
        timer = fixedRateTimer(daemon = true, period = RELOAD_PERIOD) {
            changed = false
            for (site in sites) {
                extractLink(settings.date, site.url).thenAccept { url ->
                    if (url == null) {
                        if (t.updateAppointments(site, emptyList()) && !changed) {
                            play("beethoven_kurz.wav")
                            changed = true
                        }
                    } else {
                        extractAppointments(url).thenAccept { appointments ->
                            if (t.updateAppointments(site, appointments) && !changed) {
                                play("beethoven_kurz.wav")
                                changed = true
                            }
                        }
                    }
                }
            }
        }
    }

    override fun start(primaryStage: Stage) {
        setup(primaryStage)
        showSettings()
        stage.show()
    }

    override fun stop() {
        settings.save(prefs)
    }

    companion object {
        private const val ROOT_URL =
            "https://termine-online.stadt-koeln.de/index.php?company=stadtkoeln&cur_causes=1|2|3|4|5|6|7|8|9|49"

        private const val RELOAD_PERIOD = 30000L

        @JvmStatic
        fun main(args: Array<String>) {
            launch(App::class.java, *args)
        }
    }
}