package com.example.flashblade

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import com.example.flashblade.item.FlashBladeItem

object FlashBladeMod : ModInitializer {
    const val MOD_ID = "flashblade"

    val FLASH_BLADE: Item = FlashBladeItem(Item.Settings().maxCount(1).fireproof())

    override fun onInitialize() {
        Registry.register(Registries.ITEM, id("flash_blade"), FLASH_BLADE)

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register { it.add(FLASH_BLADE) }

        AttackEntityCallback.EVENT.register(AttackEntityCallback { player, world, hand, entity, _ ->
            if (player.getStackInHand(hand).isOf(FLASH_BLADE)) {
                if (!world.isClient) {
                    val sw = world as ServerWorld
                    sw.spawnParticles(ParticleTypes.EXPLOSION, entity.x, entity.y + entity.height * 0.5, entity.z, 6, 0.2, 0.3, 0.2, 0.01)
                    world.playSound(null, entity.blockPos, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 0.4f, 2.0f)
                }
                ActionResult.SUCCESS
            } else {
                ActionResult.PASS
            }
        })

        AttackBlockCallback.EVENT.register(AttackBlockCallback { player, world, hand, pos, _ ->
            if (player.getStackInHand(hand).isOf(FLASH_BLADE)) {
                if (!world.isClient) {
                    val sw = world as ServerWorld
                    val p = pos.toCenterPos()
                    sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 8, 0.3, 0.1, 0.3, 0.0)
                    world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.6f, 1.5f)
                }
                ActionResult.SUCCESS
            } else ActionResult.PASS
        })

        UseItemCallback.EVENT.register(UseItemCallback { player, _, hand ->
            val stack = player.getStackInHand(hand)
            if (stack.isOf(FLASH_BLADE)) ActionResult.PASS else ActionResult.PASS
        })
    }

    private fun id(path: String) = Identifier(MOD_ID, path)
}

