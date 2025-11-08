package dev.orchid.petal.config

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.minecraft.client.MinecraftClient
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object ConfigIO {
    private val LOGGER = LoggerFactory.getLogger("PetalConfigIO")
    private val json = Json {
        prettyPrint = true
        allowComments = true
        ignoreUnknownKeys = true
    }

    private val defaultConfig = PetalConfig()
    private val defaultDocument = """
        // Configuration for the Petal utility mod.
        // Volumes range from 0.0 to 1.0. GUI scale and FOV values are clamped automatically.
        {
          "audio": {
            "master": 1.0,
            "music": 0.0
          },
          "video": {
            "fov": 90,
            "gui_scale": 2
          }
        }
    """.trimIndent()

    fun read(client: MinecraftClient): PetalConfig {
        val path = path(client)
        ensureFile(path)
        return runCatching {
            json.decodeFromString(PetalConfig.serializer(), path.readText())
        }.getOrElse { error ->
            LOGGER.warn("[Petal] Failed to parse petal.json, restoring defaults: ${error.message}")
            path.writeText(defaultDocument + System.lineSeparator())
            defaultConfig
        }
    }

    fun write(config: PetalConfig, client: MinecraftClient) {
        val path = path(client)
        path.parent.createDirectories()
        val body = buildString {
            appendLine("// Configuration for the Petal utility mod.")
            appendLine("// This file is rewritten automatically when malformed.")
            append(json.encodeToString(config))
        }
        path.writeText(body + System.lineSeparator())
    }

    private fun ensureFile(path: Path) {
        path.parent.createDirectories()
        if (!path.exists()) {
            path.writeText(defaultDocument + System.lineSeparator())
        }
    }

    private fun path(client: MinecraftClient): Path =
        client.runDirectory.toPath().resolve("config").resolve("petal.json")
}
