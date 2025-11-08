package dev.orchid.petal.features.autofeed

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

object AutoFeeder {
    private val controller = FeedController()

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            controller.tick(client)
        }
    }
}
