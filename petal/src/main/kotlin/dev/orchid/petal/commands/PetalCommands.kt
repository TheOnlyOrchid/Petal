package dev.orchid.petal.commands

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

object PetalCommands {
    fun register(onReload: (MinecraftClient) -> List<String>) {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                literal("petal").then(
                    literal("reload").executes { context ->
                        val client = context.source.client
                        val changes = onReload(client)
                        sendFeedback(context.source, changes)
                        1
                    },
                ),
            )
        }
    }

    private fun sendFeedback(source: FabricClientCommandSource, changes: List<String>) {
        val message = if (changes.isEmpty()) {
            Text.literal("Petal reloaded: no setting changes detected.")
        } else {
            Text.literal("Petal reloaded: ${changes.joinToString(", ")}")
        }
        source.sendFeedback(message)
    }
}
