package buergerbot

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.concurrent.Worker
import javafx.scene.web.WebEngine
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import javax.sound.sampled.AudioSystem

fun <T> ObservableValue<T>.addListenerOnce(action: (T) -> Unit) {
    val listener = object : ChangeListener<T> {
        override fun changed(observable: ObservableValue<out T>?, oldValue: T, newValue: T) {
            if (newValue == Worker.State.SUCCEEDED) {
                action(newValue)
                removeListener(this)
            }
        }
    }
    addListener(listener)
}

fun NodeList.toList() = List(length) { idx -> item(idx) }

fun WebEngine.onSuccess(action: (Document) -> Unit) {
    loadWorker.stateProperty().addListenerOnce { state ->
        if (state == Worker.State.SUCCEEDED && document != null) {
            action(document)
        }
    }
}

fun play(resource: String) = try {
    val stream = AudioSystem.getAudioInputStream(App::class.java.getResource(resource))
    val clip = AudioSystem.getClip()
    clip.open(stream)
    clip.start()
} catch (ex: Exception) {
    ex.printStackTrace()
}