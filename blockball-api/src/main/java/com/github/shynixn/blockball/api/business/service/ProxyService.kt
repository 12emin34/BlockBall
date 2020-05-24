package com.github.shynixn.blockball.api.business.service

import com.github.shynixn.blockball.api.business.enumeration.GameMode
import com.github.shynixn.blockball.api.persistence.entity.ChatBuilder
import com.github.shynixn.blockball.api.persistence.entity.Position

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
interface ProxyService {
    /**
     * Gets all available gamemodes.
     */
    val gameModes: List<String>

    /**
     * Kicks the given player with the given message.
     */
    fun <P> kickPlayer(player: P, message: String)

    /**
     * Performs a player command.
     */
    fun <P> performPlayerCommand(player: P, command: String)

    /**
     * Gets the location of the player.
     */
    fun <L, P> getPlayerLocation(player: P): L

    /**
     * Gets the name of the World the player is in.
     */
    fun <P> getWorldName(player: P): String

    /**
     * Gets the name of a player.
     */
    fun <P> getPlayerName(player: P): String

    /**
     * Gets the player uuid.
     */
    fun <P> getPlayerUUID(player: P): String

    /**
     * Sets the location of the player.
     */
    fun <L, P> setPlayerLocation(player: P, location: L)

    /**
     * Gets a copy of the player inventory.
     */
    fun <P> getPlayerInventoryCopy(player: P): Array<Any?>

    /**
     * Gets a copy of the player armor inventory.
     */
    fun <P> getPlayerInventoryArmorCopy(player: P): Array<Any?>

    /**
     * Sets the player gameMode.
     */
    fun <P> setGameMode(player: P, gameMode: GameMode)

    /**
     * Gets the player gameMode.
     */
    fun <P> getPlayerGameMode(player: P): GameMode

    /**
     * Sets the player flying.
     */
    fun <P> setPlayerFlying(player: P, enabled: Boolean)

    /**
     * Gets if the player is flying.
     */
    fun <P> getPlayerFlying(player: P): Boolean

    /**
     * Sets the player walkingSpeed.
     */
    fun <P> setPlayerWalkingSpeed(player: P, speed: Double)

    /**
     * Gets the player walkingSpeed.
     */
    fun <P> getPlayerWalkingSpeed(player: P): Double

    /**
     * Generates a new scoreboard.
     */
    fun <S> generateNewScoreboard(): S

    /**
     * Gets if the given instance is a player instance.
     */
    fun <P> isPlayerInstance(player: P): Boolean

    /**
     * Sets the player scoreboard.
     */
    fun <P, S> setPlayerScoreboard(player: P, scoreboard: S)

    /**
     * Sets the player velocity.
     */
    fun <P> setPlayerVelocity(player: P, position: Position)

    /**
     * Gets the player direction.
     */
    fun <P> getPlayerDirection(player: P): Position

    /**
     * Gets the player scoreboard.
     */
    fun <P, S> getPlayerScoreboard(player: P): S

    /**
     * Sets if the player is allowed to fly.
     */
    fun <P> setPlayerAllowFlying(player: P, enabled: Boolean)

    /**
     * Gets if the player is allowed to fly.
     */
    fun <P> getPlayerAllowFlying(player: P): Boolean

    /**
     * Gets the player level.
     */
    fun <P> getPlayerLevel(player: P): Int

    /**
     * Gets the player exp.
     */
    fun <P> getPlayerExp(player: P): Double

    /**
     * Gets the player max health.
     */
    fun <P> getPlayerMaxHealth(player: P): Double

    /**
     * Gets the player health.
     */
    fun <P> getPlayerHealth(player: P): Double

    /**
     * Gets the player hunger.
     */
    fun <P> getPlayerHunger(player: P): Int

    /**
     * Sets the given inventory items.
     */
    fun <P, I> setInventoryContents(player: P, mainInventory: Array<I>, armorInventory: Array<I>)

    /**
     * Gets a list of all online players.
     */
    fun <P> getOnlinePlayers(): List<P>

    /**
     * Sends a plugin message through the given channel.
     */
    fun <P> sendPlayerPluginMessage(player: P, channel: String, content: ByteArray)

    /**
     * Converts the given [location] to a [Position].
     */
    fun <L> toPosition(location: L): Position

    /**
     * Sends a chat message to the [sender].
     */
    fun <S> sendMessage(sender: S, chatBuilder: ChatBuilder)

    /**
     * Sends a message to the [sender].
     */
    fun <S> sendMessage(sender: S, message: String)
}