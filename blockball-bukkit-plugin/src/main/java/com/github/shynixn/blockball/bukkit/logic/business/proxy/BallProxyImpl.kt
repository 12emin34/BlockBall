@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.blockball.bukkit.logic.business.proxy

import com.github.shynixn.blockball.api.bukkit.event.*
import com.github.shynixn.blockball.api.business.enumeration.BallSize
import com.github.shynixn.blockball.api.business.enumeration.MaterialType
import com.github.shynixn.blockball.api.business.proxy.BallProxy
import com.github.shynixn.blockball.api.compatibility.BounceObject
import com.github.shynixn.blockball.api.persistence.entity.BallMeta
import com.github.shynixn.blockball.bukkit.BlockBallPlugin
import com.github.shynixn.blockball.bukkit.logic.business.extension.sync
import com.github.shynixn.blockball.bukkit.logic.business.extension.toBukkitMaterial
import com.github.shynixn.blockball.bukkit.logic.compatibility.BallData
import com.github.shynixn.blockball.bukkit.logic.compatibility.SkinHelper
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import java.util.*
import java.util.logging.Level


/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class BallProxyImpl(override val meta: BallMeta, private val design: ArmorStand, private val hitbox: ArmorStand, override val uuid: UUID = UUID.randomUUID(), private val initialOwner: LivingEntity?, override var persistent: Boolean) : BallProxy, Runnable, ConfigurationSerializable {
    companion object {
        private val excludedRelativeItems = arrayOf(MaterialType.OAK_FENCE.MinecraftNumericId, MaterialType.IRON_BARS.toBukkitMaterial(), MaterialType.GLASS_PANE.toBukkitMaterial(), MaterialType.OAK_FENCE_GATE.toBukkitMaterial(), MaterialType.NETHER_FENCE.toBukkitMaterial(), MaterialType.COBBLESTONE_WALL.toBukkitMaterial(), MaterialType.STAINED_GLASS_PANE.toBukkitMaterial(), org.bukkit.Material.SPRUCE_FENCE_GATE, org.bukkit.Material.BIRCH_FENCE_GATE, org.bukkit.Material.JUNGLE_FENCE_GATE, org.bukkit.Material.DARK_OAK_FENCE_GATE, org.bukkit.Material.ACACIA_FENCE_GATE, org.bukkit.Material.SPRUCE_FENCE, org.bukkit.Material.BIRCH_FENCE, org.bukkit.Material.JUNGLE_FENCE, org.bukkit.Material.DARK_OAK_FENCE, org.bukkit.Material.ACACIA_FENCE)
        private val skullmaterial = MaterialType.SKULL_ITEM.toBukkitMaterial()
    }

    /** Design **/
    private val plugin = JavaPlugin.getPlugin(BlockBallPlugin::class.java)
    private var backAnimation = false
    private var interactionEntity: Entity? = null
    private var counter = 20
    override var yawChange: Float = -1.0F

    /** HitBox **/
    private var knockBackBumper: Int = 0
    private var reduceVector: Vector? = null
    private var originVector: Vector? = null
    private var times: Int = 0

    /**
     * Current spinning force value.
     */
    override var spinningForce: Double = 0.0

    /**
     * Is the ball currently grabbed by some entity?
     */
    override var isGrabbed: Boolean = false
    /**
     * Is the entity dead?
     */
    override val isDead: Boolean
        get() = this.design.isDead || this.hitbox.isDead

    init {
        design.customName = "ResourceBallsPlugin"
        design.isCustomNameVisible = false

        hitbox.customName = "ResourceBallsPlugin"
        hitbox.isCustomNameVisible = false

        val event = BallSpawnEvent(this)
        Bukkit.getPluginManager().callEvent(event)
    }

    /**
     * Returns the armorstand for the design.
     */
    override fun <A> getDesignArmorstand(): A {
        return design as A
    }

    /**
     * Returns the armorstand for the hitbox.
     */
    override fun <A> getHitboxArmorstand(): A {
        return hitbox as A
    }

    /**
     * When an object implementing interface `Runnable` is used
     * to create a thread, starting the thread causes the object's
     * `run` method to be called in that separately executing
     * thread.
     *
     *
     * The general contract of the method `run` is that it may
     * take any action whatsoever.
     *
     * @see java.lang.Thread.run
     */
    override fun run() {
        try {
            this.hitbox.fireTicks = 0
            this.design.fireTicks = 0

            if (!isGrabbed) {
                checkMovementInteractions()
                if (this.meta.isRotatingEnabled) {
                    this.playRotationAnimation()
                }
            } else {
                this.hitbox.teleport(design)
            }
        } catch (e: Exception) {
            Bukkit.getLogger().log(Level.WARNING, "Entity ticking exception.", e)
        }
    }

    /**
     * Removes the ball.
     */
    override fun remove() {
        Bukkit.getPluginManager().callEvent(BallDeathEvent(this))
        this.deGrab()
        this.design.remove()
        this.hitbox.remove()
    }


    /**
     * DeGrabs the ball.
     */
    override fun deGrab() {
        if (!this.isGrabbed || this.interactionEntity == null) {
            return
        }

        val livingEntity = this.interactionEntity as LivingEntity
        livingEntity.equipment.itemInHand = null
        this.isGrabbed = false
        val itemStack = ItemStack(skullmaterial, 1, 3.toShort())

        try {
            SkinHelper.setItemStackSkin(itemStack, meta.skin)
        } catch (e1: Exception) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to deGrab entity.", e1)
        }

        this.setHelmet(itemStack)
        val vector = this.getDirection(livingEntity).normalize().multiply(3)
        this.teleport(livingEntity.location.add(vector))
    }

    /**
     * Gets the velocity of the ball.
     */
    override fun <V> getVelocity(): V {
        return hitbox.velocity as V
    }

    /**
     * Gets the last interaction entity.
     */
    override fun <L> getLastInteractionEntity(): Optional<L> {
        return Optional.ofNullable(interactionEntity as L)
    }

    /**
     * Kicks the ball by the given entity.
     * The calculated velocity can be manipulated by the BallKickEvent.
     *
     * @param entity entity
     */
    override fun <E> kickByEntity(entity: E) {
        if (entity !is Entity) {
            throw IllegalArgumentException("Entity has to be a BukkitEntity!")
        }

        if (this.isGrabbed) {
            return
        }

        val vector = hitbox.location.toVector().subtract(entity.location.toVector()).normalize()
                .multiply(meta.modifiers.horizontalKickStrengthModifier)
        this.yawChange = entity.location.yaw
        this.spinningForce = 0.0
        vector.y = 0.1 * meta.modifiers.verticalKickStrengthModifier

        val event = BallKickEvent(vector, entity, this)
        Bukkit.getPluginManager().callEvent(event)

        if (!event.isCancelled) {
            this.setVelocity(vector)

            if (entity is Player) {
                sync(plugin, 5L) {
                    spin(entity.eyeLocation.direction, event.resultVelocity)
                }
            }
        }
    }

    /**
     * Lets the ball spin to the player direction.
     */
    override fun <V> spin(playerDirection: V, resultVelocity: V) {
        if (playerDirection !is Vector) {
            throw IllegalArgumentException("PlayerDirection has to be a BukkitVelocity!")
        }

        if (resultVelocity !is Vector) {
            throw IllegalArgumentException("ResultVelocity has to be a BukkitVelocity!")
        }

        val angle = getAngle(resultVelocity, playerDirection)
        val force: Float

        force = if (angle > 0.3f && angle < 10f) {
            0.03f
        } else if (angle < -0.3f && angle > -10f) {
            -0.03f
        } else {
            return
        }

        val event = BallSpinEvent(force.toDouble(), this, true)
        Bukkit.getPluginManager().callEvent(event)

        if (!event.isCancelled) {
            spinningForce = event.spinningForce
        }
    }

    /**
     * Throws the ball by the given entity.
     * The calculated velocity can be manipulated by the BallThrowEvent.
     *
     * @param entity entity
     * @param entity entity
     */
    override fun <E> throwByEntity(entity: E) {
        if (entity !is Entity) {
            throw IllegalArgumentException("Entity has to be a BukkitEntity!")
        }

        if (interactionEntity == null || !this.isGrabbed || entity == interactionEntity) {
            return
        }

        this.deGrab()

        var vector = this.getDirection(entity).normalize()
        val y = vector.y
        vector = vector.multiply(meta.modifiers.horizontalThrowStrengthModifier)
        vector.y = y * 2.0 * meta.modifiers.verticalThrowStrengthModifier
        val event = BallThrowEvent(vector, entity, this)
        Bukkit.getPluginManager().callEvent(event)
        if (!event.isCancelled) {
            this.counter = 10
            setVelocity(vector)
        }
    }

    /**
     * Teleports the ball to the given [location].
     */
    override fun <L> teleport(location: L) {
        if (location !is Location) {
            throw IllegalArgumentException("Location has to be a BukkitLocation!")
        }

        if (this.isGrabbed) {
            this.hitbox.teleport(location)
        }
    }

    /**
     * Gets the location of the ball.
     */
    override fun <L> getLocation(): L {
        return hitbox.location as L
    }

    /**
     * Gets the optional living entity owner of the ball.
     */
    override fun <L> getOwner(): Optional<L> {
        return Optional.ofNullable(this.initialOwner as L)
    }

    /**
     * Sets the velocity of the ball.
     */
    override fun <V> setVelocity(vector: V) {
        if (vector !is Vector) {
            throw IllegalArgumentException("Vector has to be a BukkitVector!")
        }

        if (this.isGrabbed) {
            return
        }

        this.backAnimation = false
        this.spinningForce = 0.0

        if (this.meta.isRotatingEnabled) {
            this.design.headPose = EulerAngle(2.0, 0.0, 0.0)
        }

        try {
            this.times = (50 * this.meta.modifiers.rollingDistanceModifier).toInt()
            this.hitbox.velocity = vector
            val normalized = vector.clone().normalize()
            this.originVector = vector.clone()
            this.reduceVector = Vector(normalized.x / this.times, 0.0784 * meta.modifiers.gravityModifier, normalized.z / this.times)
        } catch (ignored: IllegalArgumentException) {
            // Ignore calculated velocity if it's out of range.
        }
    }

    /**
     * Lets the given living entity grab the ball.
     */
    override fun <L> grab(entity: L) {
        if (entity !is LivingEntity) {
            throw IllegalArgumentException("Entity has to be a BukkitLivingEntity!")
        }

        if (isGrabbed) {
            return
        }
        this.interactionEntity = entity

        if (entity.equipment.itemInHand == null || entity.equipment.itemInHand.type == Material.AIR) {
            val event = BallGrabEvent(entity, this)
            Bukkit.getPluginManager().callEvent(event)
            if (!event.isCancelled) {
                entity.equipment.itemInHand = design.helmet.clone()
                this.setHelmet(null)
                this.isGrabbed = true
            }
        }
    }

    override fun <V> calculateMoveSourceVectors(movementVector: V, motionVector: V, onGround: Boolean): Optional<V> {
        if (movementVector !is Vector) {
            throw IllegalArgumentException("MovementVector has to be a BukkitVector!")
        }

        if (motionVector !is Vector) {
            throw IllegalArgumentException("MotionVector has to be a BukkitVector!")
        }

        val preMoveEvent = BallPreMoveEvent(movementVector, this)
        Bukkit.getPluginManager().callEvent(preMoveEvent)

        if (preMoveEvent.isCancelled) {
            return Optional.empty()
        }

        if (this.knockBackBumper > 0) {
            this.knockBackBumper--
        }

        return if ((this.times > 0 || !onGround) && this.originVector != null) {
            this.originVector = this.originVector!!.subtract(this.reduceVector)

            if (this.times > 0) {
                motionVector.x = this.originVector!!.x
                motionVector.z = this.originVector!!.z
            }

            motionVector.y = this.originVector!!.y
            this.times--

            Optional.of(Vector(motionVector.x, motionVector.y, motionVector.z) as V)
        } else {
            Optional.of(Vector(movementVector.x, movementVector.y, movementVector.z) as V)
        }
    }

    /**
     * Calculates the knockback for the given [sourceVector] and [sourceBlock]. Uses the motion values to correctly adjust the
     * wall.
     */
    override fun <V, B> calculateKnockBack(sourceVector: V, sourceBlock: B, mot0: Double, mot2: Double, mot6: Double, mot8: Double) {
        if (sourceVector !is Vector) {
            throw IllegalArgumentException("SourceVector has to be a BukkitVector!")
        }

        if (sourceBlock !is Block) {
            throw IllegalArgumentException("SourceBlock has to be a BukkitBlock!")
        }

        var knockBackBlock: Block = sourceBlock

        when {
            mot6 > mot0 -> {
                if (this.isValidKnockBackBlock(knockBackBlock)) {
                    knockBackBlock = knockBackBlock.getRelative(BlockFace.EAST)
                }

                val n = Vector(-1, 0, 0)
                this.applyKnockBack(sourceVector, n, knockBackBlock, BlockFace.EAST)
            }
            mot6 < mot0 -> {
                if (this.isValidKnockBackBlock(knockBackBlock)) {
                    knockBackBlock = knockBackBlock.getRelative(BlockFace.WEST)
                }

                val n = Vector(1, 0, 0)
                this.applyKnockBack(sourceVector, n, knockBackBlock, BlockFace.WEST)
            }
            mot8 > mot2 -> {
                if (this.isValidKnockBackBlock(knockBackBlock)) {
                    knockBackBlock = knockBackBlock.getRelative(BlockFace.SOUTH)
                }

                val n = Vector(0, 0, -1)
                this.applyKnockBack(sourceVector, n, knockBackBlock, BlockFace.SOUTH)
            }
            mot8 < mot2 -> {
                if (this.isValidKnockBackBlock(knockBackBlock)) {
                    knockBackBlock = knockBackBlock.getRelative(BlockFace.NORTH)
                }

                val n = Vector(0, 0, 1)
                this.applyKnockBack(sourceVector, n, knockBackBlock, BlockFace.NORTH)
            }
        }
    }

    /**
     * Calculates post movement.
     */
    override fun calculatePostMovement() {
        if (originVector == null) {
            return
        }

        val postMovement = BallPostMoveEvent(this.originVector!!, true, this)
        Bukkit.getPluginManager().callEvent(postMovement)

        if (times <= 0 || spinningForce == 0.0 || this.design.isOnGround) {
            return
        }

        val event = BallSpinEvent(spinningForce, this, false)
        Bukkit.getPluginManager().callEvent(event)

        if (!event.isCancelled) {
            spinningForce = event.spinningForce

            if (spinningForce != 0.0) {
                this.originVector = calculateMagnusForce(this.originVector!!, spinningForce.toFloat())
            }
        }
    }

    /**
     * Serializes this data.
     */
    override fun serialize(): MutableMap<String, Any> {
        val data = LinkedHashMap<String, Any>()
        data["location"] = this.getLocation<Location>().serialize()
        data["meta"] = (meta as BallData).serialize()
        return data
    }

    /**
     * Applies the wall knockback.
     */
    private fun applyKnockBack(starter: Vector, n: Vector, block: Block, blockFace: BlockFace) {
        if (block.type == org.bukkit.Material.AIR) {
            return
        }

        if (this.knockBackBumper <= 0) {
            val optBounce = meta.bounceObjectController.getBounceObjectFromBlock(block) as Optional<BounceObject>
            if (optBounce.isPresent || meta.isAlwaysBounceBack) {
                var r = starter.clone().subtract(n.multiply(2 * starter.dot(n))).multiply(0.75)

                r = if (optBounce.isPresent) {
                    r.multiply(optBounce.get().bounceModifier)
                } else {
                    r.multiply(meta.modifiers.bounceModifier)
                }

                val event = BallWallCollideEvent(block, blockFace, starter.clone(), r.clone(), this)
                Bukkit.getPluginManager().callEvent(event)
                if (!event.isCancelled) {
                    this.setVelocity(r)
                    this.backAnimation = !backAnimation
                    this.knockBackBumper = 5
                }
            }
        }
    }

    /**
     * Checks movement interactions with the ball.
     */
    private fun checkMovementInteractions(): Boolean {
        if (this.counter <= 0) {
            this.counter = 2
            val hitBoxLocation = this.hitbox.location
            for (entity in design.location.chunk.entities) {
                if (entity !is ArmorStand && entity.location.distance(hitBoxLocation) < meta.hitBoxSize) {
                    val event = BallInteractEvent(entity, this)
                    Bukkit.getPluginManager().callEvent(event)
                    if (event.isCancelled)
                        return true
                    val vector = hitBoxLocation
                            .toVector()
                            .subtract(entity.location.toVector())
                            .normalize().multiply(meta.modifiers.horizontalTouchModifier)
                    vector.y = 0.1 * meta.modifiers.verticalTouchModifier

                    this.yawChange = entity.location.yaw
                    this.setVelocity(vector)
                    return true
                }
            }
        } else {
            this.counter--
        }
        return false
    }

    /**
     * Returns the launch Direction.
     *
     * @param entity entity
     * @return launchDirection
     */
    private fun getDirection(entity: Entity): Vector {
        val vector = Vector()
        val rotX = entity.location.yaw.toDouble()
        val rotY = entity.location.pitch.toDouble()
        vector.y = -Math.sin(Math.toRadians(rotY))
        val h = Math.cos(Math.toRadians(rotY))
        vector.x = -h * Math.sin(Math.toRadians(rotX))
        vector.z = h * Math.cos(Math.toRadians(rotX))
        vector.y = 0.5
        vector.add(entity.velocity)
        return vector.multiply(3)
    }

    /**
     * Plays the rotation animation.
     */
    private fun playRotationAnimation() {
        val length = this.hitbox.velocity.length()
        var angle: EulerAngle? = null

        val a = this.design.headPose
        when {
            length > 1.0 -> angle = if (this.backAnimation) {
                EulerAngle(a.x - 0.5, 0.0, 0.0)
            } else {
                EulerAngle(a.x + 0.5, 0.0, 0.0)
            }
            length > 0.1 -> angle = if (this.backAnimation) {
                EulerAngle(a.x - 0.25, 0.0, 0.0)
            } else {
                EulerAngle(a.x + 0.25, 0.0, 0.0)
            }
            length > 0.08 -> angle = if (this.backAnimation) {
                EulerAngle(a.x - 0.025, 0.0, 0.0)
            } else {
                EulerAngle(a.x + 0.025, 0.0, 0.0)
            }
        }
        if (angle != null) {
            this.design.headPose = angle
        }
    }

    /**
     * Sets the helmet.
     */
    private fun setHelmet(itemStack: ItemStack?) {
        when (meta.size!!) {
            BallSize.SMALL -> {
                this.design.isSmall = true
                this.design.helmet = itemStack
            }
            BallSize.NORMAL -> this.design.helmet = itemStack
        }
    }

    /**
     * Magnus force calculation.
     */
    private fun calculateMagnusForce(velocity: Vector, force: Float): Vector {
        val originUnit = velocity.normalize()
        val x = -originUnit.z
        val z = originUnit.x

        val newVector = velocity.add(Vector(x, 0.0, z).multiply(force))
        return newVector.multiply(velocity.length() / newVector.length())
    }

    /**
     * Calculates the angle between two vectors in two dimension (XZ Plane) <br></br>
     * If 'basis' vector is clock-wise to 'against' vector, the angle is negative.
     * @param basis The basis vector
     * @param against The vector which the angle is calculated against
     * @return The angle in the range of -180 to 180 degrees
     */
    private fun getAngle(basis: Vector, against: Vector): Double {
        val dot = basis.x * against.x + basis.z * against.z
        val det = basis.x * against.z - basis.z * against.x

        return Math.atan2(det, dot)
    }

    /**
     * Gets if the given block is a valid knockback block.
     */
    private fun isValidKnockBackBlock(block: Block): Boolean {
        val material = block.type
        for (i in excludedRelativeItems) {
            if (i == material) {
                return false
            }
        }

        return true
    }
}