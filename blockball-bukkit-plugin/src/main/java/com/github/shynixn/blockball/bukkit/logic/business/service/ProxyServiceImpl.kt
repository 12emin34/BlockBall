@file:Suppress("UNCHECKED_CAST", "UNCHECKED_CAST", "DEPRECATION")

package com.github.shynixn.blockball.bukkit.logic.business.service

import com.github.shynixn.blockball.api.business.enumeration.Version
import com.github.shynixn.blockball.api.business.proxy.PluginProxy
import com.github.shynixn.blockball.api.business.service.ProxyService
import com.github.shynixn.blockball.api.persistence.entity.ChatBuilder
import com.github.shynixn.blockball.api.persistence.entity.Position
import com.github.shynixn.blockball.bukkit.logic.business.extension.*
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.Scoreboard
import java.util.*
import java.util.logging.Level
import kotlin.collections.ArrayList

/**
 * Created by Shynixn 2019.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2019 by Shynixn
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
class ProxyServiceImpl @Inject constructor(private val pluginProxy: PluginProxy) : ProxyService {

    /**
     * Gets the name of the World the player is in.
     */
    override fun <P> getWorldName(player: P): String {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        return player.world.name
    }

    /**
     * Gets the name of a player.
     */
    override fun <P> getPlayerName(player: P): String {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        return player.name
    }

    /**
     * Gets the player uuid.
     */
    override fun <P> getPlayerUUID(player: P): String {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        return player.uniqueId.toString()
    }

    /**
     * Gets a list of all online players.
     */
    override fun <P> getOnlinePlayers(): List<P> {
        val players = ArrayList<Player>()

        for (world in Bukkit.getWorlds()) {
            players.addAll(world.players)
        }

        return players as List<P>
    }

    /**
     * Sends a plugin message through the given channel.
     */
    override fun <P> sendPlayerPluginMessage(player: P, channel: String, content: ByteArray) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        player.sendPluginMessage(this.pluginProxy as Plugin, "BungeeCord", content)
    }

    /**
     * Gets all available gamemodes.
     */
    override val gameModes: List<String>
        get() {
            return GameMode.values().map { g -> g.name }
        }

    /**
     * Teleports the player to the given location.
     */
    override fun <P, L> teleport(player: P, location: L) {
        require(player is Player)

        if (location is Location) {
            player.teleport(location)
            return
        }

        if (location is Position) {
            player.teleport(location.toLocation())
        }
    }

    /**
     * Kicks the given player with the given message.
     */
    override fun <P> kickPlayer(player: P, message: String) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        player.kickPlayer(message)
    }

    /**
     * Performs a player command.
     */
    override fun <P> performPlayerCommand(player: P, command: String) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        player.performCommand(command)
    }

    /**
     * Performs a server command.
     */
    override fun performServerCommand(command: String) {
        Bukkit.getServer()
            .dispatchCommand(Bukkit.getConsoleSender(), command)
    }

    /**
     * Sets the location of the player.
     */
    override fun <L, P> setPlayerLocation(player: P, location: L) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        if (location is Position) {
            player.teleport(location.toLocation())
            return
        }

        if (location is Location) {
            player.teleport(location)
            return
        }

        throw IllegalArgumentException("Location has to be a position or location!")
    }

    /**
     * Gets the location of the player.
     */
    override fun <L, P> getPlayerLocation(player: P): L {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        return player.location as L
    }

    /**
     * Gets a copy of the player inventory.
     */
    override fun <P> getPlayerInventoryCopy(player: P): Array<Any?> {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        return player.inventory.contents.clone() as Array<Any?>
    }

    /**
     * Gets a copy of the player armor inventory.
     */
    override fun <P> getPlayerInventoryArmorCopy(player: P): Array<Any?> {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        return player.inventory.armorContents.clone() as Array<Any?>
    }

    /**
     * Sets the player gameMode.
     */
    override fun <P> setGameMode(player: P, gameMode: com.github.shynixn.blockball.api.business.enumeration.GameMode) {
        require(player is Player)
        player.gameMode = GameMode.values().first { g -> g.name == gameMode.name }
    }

    /**
     * Gets the player gameMode.
     */
    override fun <P> getPlayerGameMode(player: P): com.github.shynixn.blockball.api.business.enumeration.GameMode {
        require(player is Player)
        return com.github.shynixn.blockball.api.business.enumeration.GameMode.values()
            .first { g -> g.name == player.gameMode.name }
    }

    /**
     * Sets the player flying.
     */
    override fun <P> setPlayerFlying(player: P, enabled: Boolean) {
        require(player is Player)
        player.isFlying = enabled
    }

    /**
     * Gets if the player is flying.
     */
    override fun <P> getPlayerFlying(player: P): Boolean {
        require(player is Player)
        return player.isFlying
    }

    /**
     * Sets the player walkingSpeed.
     */
    override fun <P> setPlayerWalkingSpeed(player: P, speed: Double) {
        require(player is Player)
        player.walkSpeed = speed.toFloat()
    }

    /**
     * Gets the player walkingSpeed.
     */
    override fun <P> getPlayerWalkingSpeed(player: P): Double {
        require(player is Player)
        return player.walkSpeed.toDouble()
    }

    /**
     * Generates a new scoreboard.
     */
    override fun <S> generateNewScoreboard(): S {
        return Bukkit.getScoreboardManager()!!.newScoreboard as S
    }

    /**
     * Gets if the given instance is a player instance.
     */
    override fun <P> isPlayerInstance(player: P): Boolean {
        return player != null && player is Player
    }

    /**
     * Sets the player scoreboard.
     */
    override fun <P, S> setPlayerScoreboard(player: P, scoreboard: S) {
        require(player is Player)
        require(scoreboard is Scoreboard)
        player.scoreboard = scoreboard
    }

    /**
     * Sets the player velocity.
     */
    override fun <P> setPlayerVelocity(player: P, position: Position) {
        require(player is Player)
        player.velocity = position.toVector()
    }

    /**
     * Gets the player direction.
     */
    override fun <P> getPlayerDirection(player: P): Position {
        require(player is Player)
        return player.location.direction.toPosition()
    }

    /**
     * Gets the player scoreboard.
     */
    override fun <P, S> getPlayerScoreboard(player: P): S {
        require(player is Player)
        return player.scoreboard as S
    }

    /**
     * Sets if the player is allowed to fly.
     */
    override fun <P> setPlayerAllowFlying(player: P, enabled: Boolean) {
        require(player is Player)
        player.allowFlight = enabled
    }

    /**
     * Gets if the player is allowed to fly.
     */
    override fun <P> getPlayerAllowFlying(player: P): Boolean {
        require(player is Player)
        return player.allowFlight
    }

    /**
     * Gets the player level.
     */
    override fun <P> getPlayerLevel(player: P): Int {
        require(player is Player)
        return player.level
    }

    /**
     * Gets the player exp.
     */
    override fun <P> getPlayerExp(player: P): Double {
        require(player is Player)
        return player.exp.toDouble()
    }

    /**
     * Sets the player exp.
     */
    override fun <P> setPlayerExp(player: P, exp: Double) {
        require(player is Player)
        player.exp = exp.toFloat()
    }

    /**
     * Sets the player level.
     */
    override fun <P> setPlayerLevel(player: P, level: Int) {
        require(player is Player)
        player.level = level
    }

    /**
     * Gets the player max health.
     */
    override fun <P> getPlayerMaxHealth(player: P): Double {
        require(player is Player)
        return player.maxHealth
    }

    /**
     * Gets the player health.
     */
    override fun <P> getPlayerHealth(player: P): Double {
        require(player is Player)
        return player.health
    }

    /**
     * Gets the player hunger.
     */
    override fun <P> getPlayerHunger(player: P): Int {
        require(player is Player)
        return player.foodLevel
    }

    /**
     * Sets the given inventory items.
     */
    override fun <P, I> setInventoryContents(player: P, mainInventory: Array<I>, armorInventory: Array<I>) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        player.inventory.contents = mainInventory.clone().map { d -> d as ItemStack? }.toTypedArray()
        player.inventory.setArmorContents(armorInventory.clone().map { d -> d as ItemStack? }.toTypedArray())
        player.inventory.updateInventory()
    }

    /**
     * Converts the given [location] to a [Position].
     */
    override fun <L> toPosition(location: L): Position {
        if (location !is Location) {
            throw IllegalArgumentException("Location has to be a BukkitLocation!")
        }

        return location.toPosition()
    }

    /**
     * Converts the given [position] to a [Location].
     */
    override fun <L> toLocation(position: Position): L {
        return position.toLocation() as L
    }

    /**
     * Converts the given [position] to a [Vector].
     */
    override fun <V> toVector(position: Position): V {
        return position.toVector() as V
    }

    /**
     * Sets the location direction.
     */
    override fun <L> setLocationDirection(location: L, position: Position) {
        require(location is Location)
        location.direction = position.toVector()
    }


    /**
     * Gets a list of players in the given world of the given location.
     */
    override fun <P, L> getPlayersInWorld(location: L): List<P> {
        val actualLocation = when (location) {
            is Location -> {
                location
            }
            is Position -> {
                location.toLocation()
            }
            else -> {
                throw IllegalArgumentException("Parameter $location is not a location!")
            }
        }

        return actualLocation.world!!.players as List<P>
    }

    /**
     * Has player permission?
     */
    override fun <P> hasPermission(player: P, permission: String): Boolean {
        require(player is Player)
        return player.hasPermission(permission)
    }

    /**
     * Sends a chat message to the [sender].
     */
    override fun <S> sendMessage(sender: S, chatBuilder: ChatBuilder) {
        if (sender !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        try {
            val clazz: Class<*> = if (pluginProxy.getServerVersion() == Version.VERSION_1_8_R1) {
                findClazz("net.minecraft.server.VERSION.ChatSerializer")
            } else {
                findClazz("net.minecraft.server.VERSION.IChatBaseComponent\$ChatSerializer")
            }

            val packetClazz = findClazz("net.minecraft.server.VERSION.PacketPlayOutChat")
            val chatBaseComponentClazz = findClazz("net.minecraft.server.VERSION.IChatBaseComponent")
            val chatComponent = clazz.getDeclaredMethod("a", String::class.java).invoke(null, chatBuilder.toString())

            val packet = when {
                pluginProxy.getServerVersion().isVersionSameOrGreaterThan(Version.VERSION_1_16_R1) -> {
                    val systemUtilsClazz = findClazz("net.minecraft.server.VERSION.SystemUtils")
                    val defaultUUID = systemUtilsClazz.getDeclaredField("b").get(null) as UUID
                    val chatEnumMessage = findClazz("net.minecraft.server.VERSION.ChatMessageType")
                    packetClazz.getDeclaredConstructor(chatBaseComponentClazz, chatEnumMessage, UUID::class.java)
                        .newInstance(chatComponent, chatEnumMessage.enumConstants[0], defaultUUID)
                }
                pluginProxy.getServerVersion().isVersionSameOrGreaterThan(Version.VERSION_1_12_R1) -> {
                    val chatEnumMessage = findClazz("net.minecraft.server.VERSION.ChatMessageType")
                    packetClazz.getDeclaredConstructor(chatBaseComponentClazz, chatEnumMessage)
                        .newInstance(chatComponent, chatEnumMessage.enumConstants[0])
                }
                else -> {
                    packetClazz.getDeclaredConstructor(
                        chatBaseComponentClazz,
                        Byte::class.javaPrimitiveType as Class<*>
                    )
                        .newInstance(chatComponent, 0.toByte())
                }
            }

            sender.sendPacket(packet)
        } catch (e: Exception) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to send packet.", e)
        }
    }

    /**
     * Sends a message to the [sender].
     */
    override fun <S> sendMessage(sender: S, message: String) {
        if (sender is CommandSender) {
            sender.sendMessage(message)
            return
        }

        if (sender is Player) {
            sender.sendMessage(message)
        }
    }

    /**
     * Sets the player max health.
     */
    override fun setPlayerMaxHealth(player: Any, health: Double) {
        require(player is Player)
        player.maxHealth = health
    }

    /**
     * Sets the player health.
     */
    override fun setPlayerHealth(player: Any, health: Double) {
        require(player is Player)
        player.health = health
    }

    /**
     * Sets the player hunger.
     */
    override fun setPlayerHunger(player: Any, hunger: Int) {
        require(player is Player)
        player.foodLevel = hunger
    }
}