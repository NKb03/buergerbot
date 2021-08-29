package buergerbot

import com.sun.webkit.dom.HTMLDocumentImpl
import com.sun.webkit.dom.HTMLInputElementImpl
import javafx.application.Platform
import javafx.scene.web.WebEngine
import org.w3c.dom.Document
import org.w3c.dom.html.HTMLAnchorElement
import java.net.URL
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import java.util.concurrent.CompletableFuture


data class Site(val name: String, val url: String)

data class Appointment(val time: String, val url: String)

private fun load(url: String): CompletableFuture<Document> {
    val future = CompletableFuture<Document>()
    lateinit var engine: WebEngine
    Platform.runLater {
        engine = WebEngine()
        engine.onSuccess {
            future.complete(engine.document)
        }
        engine.load(url)
    }
    return future
}


fun findSubmitButton(document: Document): HTMLInputElementImpl = document
    .getElementsByTagName("input").toList()
    .find { it?.attributes?.getNamedItem("type")?.textContent == "submit" }!!
        as HTMLInputElementImpl

val centers = listOf(
    "Chorweiler",
    "Ehrenfeld",
    "Innenstadt",
    "Kalk",
    "Lindenthal",
    "Mülheim",
    "Nippes",
    "Porz",
    "Rodenkirchen",
    "Temporäre Ausweise- und Meldestelle"
)

fun extractSites(d: Document): List<Site> = d.getElementsByTagName("a").toList()
    .filterIsInstance<HTMLAnchorElement>()
    .filter { it.attributes?.getNamedItem("title")?.textContent?.startsWith("Standort") == true }
    .map { l -> Site(l.firstChild.textContent, l.href) }

fun extractLink(date: LocalDate, url: String): CompletableFuture<String?> = load(url).thenApply { d ->
    val (month, year) = d.getElementsByTagName("abbr").toList()
        .find { n -> n.textContent.takeLast(4).all { c -> c.isDigit() } }
        ?.textContent?.split(' ') ?: return@thenApply null
    val todaysMonth = date.month.getDisplayName(TextStyle.FULL, Locale.GERMAN)
    if (year.toInt() != date.year || month != todaysMonth) return@thenApply null
    d.getElementsByTagName("a").toList()
        .filterIsInstance<HTMLAnchorElement>()
        .filter { it.attributes.getNamedItem("class")?.textContent == "nat_calendar_weekday_bookable" }
        .find { link ->
            val day = link.textContent.drop(19).take(2).removePrefix("0").toInt()
            day == date.dayOfMonth
        }?.href
}

fun extractAppointments(url: String): CompletableFuture<List<Appointment>> = load(url).thenApply { d ->
    d.getElementsByTagName("a").toList()
        .filterIsInstance<HTMLAnchorElement>()
        .filter { l -> l.firstChild?.nodeName?.equals("abbr", ignoreCase = true) == true }
        .filter { l -> l.firstChild?.attributes?.getNamedItem("title")?.textContent?.startsWith("Termin um") == true }
        .map { l -> Appointment(l.firstChild.textContent, l.href) }
}
