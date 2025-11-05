package com.example.flashblade.item

import net.minecraft.entity.LivingEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraft.entity.player.PlayerEntity
import kotlin.math.cos
import kotlin.math.sin

class FlashBladeItem(settings: Settings) : Item(settings) {
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        if (!world.isClient) {
            val sw = world as ServerWorld
            val forward = user.getRotationVec(1.0f).normalize()
            val origin = user.eyePos

            val length = 8.0
            val steps = 32
            for (i in 0..steps) {
                val t = i / steps.toDouble()
                val pos = origin.add(forward.multiply(t * length))
                val swirl = Vec3d(
                    cos(t * 12.0) * 0.4,
                    sin(t * 10.0) * 0.2,
                    sin(t * 12.0) * 0.4
                )
                val p = pos.add(swirl)
                sw.spawnParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 2, 0.05, 0.05, 0.05, 0.0)
                sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0)
            }

            val ringCount = 48
            val radius = 3.0
            for (i in 0 until ringCount) {
                val angle = (i / ringCount.toDouble()) * 2.0 * Math.PI
                val p = user.pos.add(cos(angle) * radius, 0.1, sin(angle) * radius)
                sw.spawnParticles(ParticleTypes.SONIC_BOOM, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0)
            }

            world.playSound(null, user.blockPos, SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.7f, 1.2f)

            val coneRadius = 5.0
            val box = Box(user.pos.add(-coneRadius, -1.0, -coneRadius), user.pos.add(coneRadius, 2.0, coneRadius))
            val entities = sw.getEntitiesByClass(LivingEntity::class.java, box) { it != user && it.isAlive }
            val dir = forward
            for (e in entities) {
                val to = e.pos.add(0.0, e.height * 0.5, 0.0).subtract(user.eyePos)
                val dist = to.length()
                if (dist <= coneRadius) {
                    val angleCos = dir.dotProduct(to.normalize())
                    if (angleCos > 0.5) {
                        val damage = 3.0f + ((coneRadius - dist) * 0.6f).toFloat()
                        e.damage(sw.damageSources.playerAttack(user), damage)
                        val knock = dir.multiply(0.8)
                        e.takeKnockback(0.6, -knock.x, -knock.z)
                        sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, e.x, e.y + e.height * 0.5, e.z, 3, 0.2, 0.2, 0.2, 0.0)
                    }
                }
            }

            user.itemCooldownManager.set(this, 10)
        } else {
            val origin = user.eyePos
            val fwd = user.getRotationVec(1.0f)
            for (i in 0..10) {
                val t = i / 10.0
                val p = origin.add(fwd.multiply(t * 2.0))
                world.addParticle(ParticleTypes.CRIT, p.x, p.y, p.z, 0.0, 0.0, 0.0)
            }
        }
        user.swingHand(hand, true)
        return TypedActionResult.success(stack, world.isClient)
    }
}

