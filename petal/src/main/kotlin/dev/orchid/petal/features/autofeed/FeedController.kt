package dev.orchid.petal.features.autofeed

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.item.ItemStack
import org.slf4j.LoggerFactory
import kotlin.math.max

class FeedController {
    private val logger = LoggerFactory.getLogger("PetalAutoFeeder")
    private var tracked: TrackingState? = null

    fun tick(client: MinecraftClient) {
        val player = client.player ?: run { tracked = null; return }

        if (!player.canUseAutoFeed()) { tracked = null; return }

        val hungerLevel = player.hungerManager.foodLevel

        tracked = tracked?.validate(player)
        if (tracked == null) {
            tracked = FoodFinder.findFirstFood(player.inventory)?.let { c ->
                TrackingState(candidate = c, startingHunger = hungerLevel)
            }
        }

        val state = tracked ?: return
        if (!state.isReady(hungerLevel)) return

        if (!consume(player, state)) { tracked = null; return }

        tracked = null
    }

    private fun consume(player: ClientPlayerEntity, state: TrackingState): Boolean {
        val stack = player.inventory.getStack(state.candidate.slot)
        if (stack.isEmpty || stack.item != state.candidate.item) return false

        val food = state.candidate.food
        player.hungerManager.add(food.nutrition, food.saturation)
        stack.decrement(1)
        if (stack.isEmpty) {
            player.inventory.setStack(state.candidate.slot, ItemStack.EMPTY)
        }

        logger.info("[Petal] AutoFeeder served ${state.candidate.label} from slot ${state.candidate.slot}.")
        return true
    }

    private fun ClientPlayerEntity.canUseAutoFeed(): Boolean = !isCreative && !isSpectator

    private data class TrackingState(
        val candidate: FoodCandidate,
        val startingHunger: Int,
    ) {
        fun validate(player: ClientPlayerEntity): TrackingState? {
            val stack = player.inventory.getStack(candidate.slot)
            if (stack.isEmpty || stack.item != candidate.item) return null
            val current = player.hungerManager.foodLevel
            val updatedStart = max(startingHunger, current)
            return if (updatedStart == startingHunger) this else copy(startingHunger = updatedStart)
        }

        fun isReady(currentFoodLevel: Int): Boolean {
            val drop = startingHunger - currentFoodLevel
            return drop >= candidate.food.nutrition
        }
    }
}
