package dev.orchid.petal.features.autofeed

import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.FoodComponent
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

object FoodFinder {
    fun findFirstFood(inventory: PlayerInventory): FoodCandidate? {
        for (slot in 0 until inventory.size()) {
            val stack = inventory.getStack(slot)
            val component = stack.get(DataComponentTypes.FOOD) ?: continue
            return FoodCandidate(
                slot = slot,
                item = stack.item,
                food = component,
                label = stack.name.string,
            )
        }
        return null
    }

    private fun ItemStack.foodComponent(): FoodComponent? =
        if (isEmpty) null else get(DataComponentTypes.FOOD)
}

data class FoodCandidate(
    val slot: Int,
    val item: Item,
    val food: FoodComponent,
    val label: String,
)
