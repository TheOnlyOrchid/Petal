package dev.orchid.petal.boot

import net.minecraft.client.MinecraftClient
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

object FirstRunWorldMaker {
    private val LOGGER = LoggerFactory.getLogger("PetalWorldMaker")
    private val executor = Executors.newSingleThreadExecutor {
        Thread(it, "Petal-WorldScanner").apply { isDaemon = true }
    }
    private val running = AtomicBoolean(false)
    private const val WORLD_NAME = "Petal Superflat"

    fun checkAndCreate(client: MinecraftClient) {
        if (!running.compareAndSet(false, true)) {
            return
        }

        val savesDir = client.runDirectory.toPath().resolve("saves")
        executor.execute {
            try {
                if (hasExistingWorlds(savesDir)) {
                    LOGGER.info("[Petal] Existing singleplayer worlds detected, skip bootstrap world.")
                } else {
                    LOGGER.info(
                        "[Petal] No existing worlds detected. Create a \"$WORLD_NAME\" superflat world for the best portfolio demo."
                    )
                }
            } catch (t: Throwable) {
                LOGGER.error("[Petal] Failed while scanning for existing worlds", t)
            } finally {
                running.set(false)
            }
        }
    }

    private fun hasExistingWorlds(savesDir: Path): Boolean {
        if (!savesDir.exists()) return false
        return savesDir.listDirectoryEntries().any {
            it.isDirectory() && Files.exists(it.resolve("level.dat"))
        }
    }
}
