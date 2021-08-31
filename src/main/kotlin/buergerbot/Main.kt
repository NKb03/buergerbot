package buergerbot

import buergerbot.Main.OS.*
import javafx.application.Application
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Paths
import java.util.prefs.Preferences
import kotlin.system.exitProcess

object Main {
    private val os = determineOS()
    private val workingDirectory = File(System.getProperty("user.home"), ".buergerbot").also { it.mkdir() }
    private val self = File(javaClass.protectionDomain.codeSource.location.toURI())

    @JvmStatic
    fun main(args: Array<String>) {
        when (args.firstOrNull()) {
            "launch" -> Application.launch(App::class.java)
            null -> {
                val sdk = file("javafx-sdk-11.0.2", "lib")
                if (!sdk.isDirectory) downloadJavaFX()
                run(
                    "java",
                    "--module-path", sdk.absolutePath,
                    "--add-modules", "javafx.controls",
                    "--add-modules", "javafx.web",
                    "--add-opens", "javafx.web/com.sun.webkit.dom=ALL-UNNAMED",
                    "-jar", self.absolutePath,
                    "launch"
                )
            }
        }
    }

    private fun downloadJavaFX() {
        download("https://gluonhq.com/download/javafx-11-0-2-sdk-$os/", "javafx.zip")
        if (os == Windows) {
            download("https://www.7-zip.org/a/7z1900-x64.exe", "7z.exe")
            run("tar", "-xf", "javafx.zip")
        } else {
            download("https://oss.oracle.com/el4/unzip/unzip.tar", "unzip.tar")
            run("tar", "-xf", "unzip.tar")
            run("chmod", "+x", "unzip")
            run("unzip", "javafx.zip")
            rm("unzip.tar", "unzip")
        }
    }

    private fun determineOS(): OS {
        val osName = System.getProperty("os.name").lowercase()
        return when {
            "win" in osName -> Windows
            "mac" in osName -> Mac
            "lin" in osName -> Linux
            else -> fail("Unknown os $osName")
        }
    }

    private fun run(command: String, vararg arguments: String) {
        val exitCode = ProcessBuilder(command, *arguments)
            .directory(workingDirectory)
            .inheritIO()
            .start()
            .waitFor()
        if (exitCode != 0) {
            fail("command $command ${arguments.joinToString(" ")} exited with non-zero exit code")
        }
    }

    private fun rm(vararg fileNames: String) {
        for (fileName in fileNames) file(fileName).delete()
    }

    private fun file(vararg path: String): File = path.fold(workingDirectory, File::resolve)

    private fun download(url: String, fileName: String) {
        val dest = file(fileName)
        if (dest.exists()) return
        val rbc = Channels.newChannel(URL(url).openStream())
        val output = FileOutputStream(dest).channel
        println("Downloading $fileName")
        output.transferFrom(rbc, 0, Long.MAX_VALUE)
        println("Download finished")
    }

    private fun fail(msg: String): Nothing {
        System.err.println(msg)
        exitProcess(1)
    }

    enum class OS {
        Windows, Mac, Linux
    }
}