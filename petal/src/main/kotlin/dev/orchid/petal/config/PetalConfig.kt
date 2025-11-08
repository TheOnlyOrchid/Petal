package dev.orchid.petal.config

import kotlinx.serialization.Serializable
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.Window

@Serializable
data class Audio(
    val master: Double = 1.0,
    val music: Double = 0.0,
)

@Serializable
data class Video(
    val fov: Int = 90,
    val gui_scale: Int = 2,
)

@Serializable
data class PetalConfig(
    val audio: Audio = Audio(),
    val video: Video = Video(),
)

data class SettingAdjustment(
    val key: String,
    val original: String,
    val adjusted: String,
)

data class ClampedConfig(
    val config: PetalConfig,
    val adjustments: List<SettingAdjustment>,
)

object ConfigValidator {
    private const val MIN_VOLUME = 0.0
    private const val MAX_VOLUME = 1.0
    private const val MIN_FOV = 30
    private const val MAX_FOV = 120
    private const val MIN_GUI_SCALE = 0
    private const val MAX_GUI_SCALE = 8

    fun clamp(raw: PetalConfig, client: MinecraftClient): ClampedConfig {
        val window: Window = client.window
        val adjustments = mutableListOf<SettingAdjustment>()

        fun clampVolume(label: String, value: Double): Double {
            val clamped = value.coerceIn(MIN_VOLUME, MAX_VOLUME)
            if (clamped != value) {
                adjustments += SettingAdjustment("audio.$label", "%.2f".format(value), "%.2f".format(clamped))
            }
            return clamped
        }

        val master = clampVolume("master", raw.audio.master)
        val music = clampVolume("music", raw.audio.music)

        fun clampFov(value: Int): Int {
            val clamped = value.coerceIn(MIN_FOV, MAX_FOV)
            if (clamped != value) {
                adjustments += SettingAdjustment("video.fov", value.toString(), clamped.toString())
            }
            return clamped
        }

        fun clampGuiScale(value: Int): Int {
            val unicode = client.forcesUnicodeFont()
            val maxScale = calculateMaxGuiScale(window, unicode)
            val clamped = value.coerceIn(MIN_GUI_SCALE, maxScale)
            if (clamped != value) {
                adjustments += SettingAdjustment("video.gui_scale", value.toString(), clamped.toString())
            }
            return clamped
        }

        val clampedConfig = PetalConfig(
            audio = Audio(master = master, music = music),
            video = Video(
                fov = clampFov(raw.video.fov),
                gui_scale = clampGuiScale(raw.video.gui_scale),
            ),
        )

        return ClampedConfig(clampedConfig, adjustments)
    }

    private fun calculateMaxGuiScale(window: Window, unicode: Boolean): Int =
        window.calculateScaleFactor(MAX_GUI_SCALE, unicode).coerceAtLeast(MIN_GUI_SCALE)
}
