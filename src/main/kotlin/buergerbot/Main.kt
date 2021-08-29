package buergerbot

import buergerbot.Main.OS.*
import javafx.application.Application
import java.awt.GraphicsEnvironment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.channels.Channels
import java.util.prefs.Preferences
import kotlin.system.exitProcess

object Main {
    private val os = determineOS()
    private val workingDirectory = File(System.getProperty("user.home"), ".javafx").also { it.mkdir() }
    private val prefs = Preferences.userNodeForPackage(javaClass)
    private val self = javaClass.protectionDomain.codeSource.location.toURI().path

    @JvmStatic
    fun main(args: Array<String>) {
        when (args.firstOrNull()) {
            "launch" -> Application.launch(App::class.java)
            null -> {
                val sdkRoot = prefs.get("JAVAFX_SDK", System.getenv("JAVAFX_SDK"))
                    ?.takeIf(this::sdkRecognized)
                    ?: setupJavaFX()
                val lib = File(sdkRoot, "lib").absolutePath
                run(
                    "java",
                    "--module-path", lib,
                    "--add-modules", "javafx.controls",
                    "--add-modules", "javafx.web",
                    "--add-opens", "javafx.web/com.sun.webkit.dom=ALL-UNNAMED",
                    "-jar", self,
                    "launch"
                )
            }
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

    private fun setupJavaFX(): String {
        println("Do you have the JavaFX SDK installed? ([y]es/[n]o)")
        val sdkRoot = if (readLine() != "y") {
            download("https://gluonhq.com/download/javafx-11-0-2-sdk-$os/", "javafx.zip")
            if (os == Windows) {
                download("https://www.7-zip.org/a/7z1900-x64.exe", "7z.exe")
                run("7z.exe", "x", "javafx.zip")
            } else {
                download("https://oss.oracle.com/el4/unzip/unzip.tar", "unzip.tar")
                run("tar", "-xf", "unzip.tar")
                run("chmod", "+x", "unzip")
                run("unzip", "javafx.zip")
            }
            workingDirectory.resolve("javafx-sdk-11.0.2").absolutePath
        } else {
            println("Where is your JavaFX SDK located?")
            val location = readLine() ?: fail("invalid path")
            if (!sdkRecognized(location)) fail("JavaFX SDK not recognized")
            location
        }
        prefs.put("JAVAFX_SDK", sdkRoot)
        return sdkRoot
    }

    private fun sdkRecognized(path: String): Boolean {
        val file = File(path)
        try {
            file.canonicalPath
        } catch (ex: IOException) {
            return false
        }
        return file.resolve("lib").resolve("javafx.controls.jar").exists()
    }

    private fun download(url: String, fileName: String) {
        val rbc = Channels.newChannel(URL(url).openStream())
        val dest = workingDirectory.resolve(fileName)
        if (dest.exists()) return
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