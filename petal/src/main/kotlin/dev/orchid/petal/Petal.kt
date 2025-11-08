package dev.orchid.petal

import dev.orchid.petal.boot.FirstRunWorldMaker
import dev.orchid.petal.commands.PetalCommands
import dev.orchid.petal.config.ConfigIO
import dev.orchid.petal.config.ConfigValidator
import dev.orchid.petal.config.PetalConfig
import dev.orchid.petal.features.autofeed.AutoFeeder
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.TitleScreen
import net.minecraft.server.integrated.IntegratedServerLoader
import net.minecraft.sound.SoundCategory
import net.minecraft.world.GameRules
import net.minecraft.world.gen.WorldPreset
import net.minecraft.world.level.LevelInfo
import net.minecraft.world.level.WorldGenSettings
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

@Environment(EnvType.CLIENT)
class Petal : ClientModInitializer {
    private val LOGGER = LoggerFactory.getLogger("Petal")
    private val appliedOnce = AtomicBoolean(false)
    private var lastConfig: PetalConfig = PetalConfig()
    private val mc = MinecraftClient.getInstance()

    override fun onInitializeClient() {
        LOGGER.info("[Petal] Orchid Studio watermark active - Petal utility online.")

        ScreenEvents.AFTER_INIT.register { client, screen, _, _ ->
            if (screen is TitleScreen && appliedOnce.compareAndSet(false, true)) {
                loadAndApply(client, "startup")
                FirstRunWorldMaker.checkAndCreate(client)
            }
        }

        PetalCommands.register { client ->
            loadAndApply(client, "command")
        }

        AutoFeeder.register()
    }

    private fun loadAndApply(client: MinecraftClient, reason: String): List<String> {
        val raw = ConfigIO.read(client)
        val (config, adjustments) = ConfigValidator.clamp(raw, client)
        lastConfig = config

        adjustments.forEach {
            LOGGER.warn("[Petal] Clamped ${it.key}: ${it.original} -> ${it.adjusted}")
        }

        val changes = applyOptions(client, config)
        if (changes.isEmpty()) {
            LOGGER.info("[Petal] Applied config ($reason) with no option changes.")
        } else {
            LOGGER.info("[Petal] Applied config ($reason): ${changes.joinToString(", ")}")
        }
        client.options.write()
        return changes
    }

    private fun applyOptions(client: MinecraftClient, config: PetalConfig): List<String> {
        val options = client.options
        val changes = mutableListOf<String>()

        val masterOption = options.getSoundVolumeOption(SoundCategory.MASTER)
        val masterTarget = config.audio.master
        val masterBefore = formatVolume(masterOption.value)
        val masterAfter = formatVolume(masterTarget)
        if (!volumeEquals(masterOption.value, masterTarget)) {
            masterOption.value = masterTarget
            LOGGER.info("[Petal] masterVolume $masterBefore -> $masterAfter")
            changes += "masterVolume $masterBefore->$masterAfter"
        }

        val musicOption = options.getSoundVolumeOption(SoundCategory.MUSIC)
        val musicTarget = config.audio.music
        val musicBefore = formatVolume(musicOption.value)
        val musicAfter = formatVolume(musicTarget)
        if (!volumeEquals(musicOption.value, musicTarget)) {
            musicOption.value = musicTarget
            LOGGER.info("[Petal] musicVolume $musicBefore -> $musicAfter")
            changes += "musicVolume $musicBefore->$musicAfter"
        }

        val fovOption = options.fov
        val fovBefore = fovOption.value
        val fovTarget = config.video.fov
        if (fovBefore != fovTarget) {
            fovOption.value = fovTarget
            LOGGER.info("[Petal] fov $fovBefore -> $fovTarget")
            changes += "fov $fovBefore->$fovTarget"
        }

        val guiOption = options.guiScale
        val guiBefore = guiOption.value
        val guiTarget = config.video.gui_scale
        if (guiBefore != guiTarget) {
            guiOption.value = guiTarget
            LOGGER.info("[Petal] guiScale $guiBefore -> $guiTarget")
            changes += "guiScale $guiBefore->$guiTarget"
            client.onResolutionChanged()
        }

        return changes
    }

    private fun formatVolume(value: Double): String = "%.2f".format(value)

    private fun volumeEquals(first: Double, second: Double): Boolean =
        abs(first - second) < 0.0
}
