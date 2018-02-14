package com.github.shynixn.blockball.bukkit.logic.business.listener

import com.github.shynixn.ball.api.bukkit.business.event.BallInteractEvent
import com.github.shynixn.blockball.bukkit.logic.business.controller.GameRepository
import com.github.shynixn.blockball.bukkit.logic.business.entity.game.SoccerGame
import com.google.inject.Inject
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

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
class GameListener @Inject constructor(plugin: Plugin) : SimpleListener(plugin) {
    @Inject
    private var gameController: GameRepository? = null

    /**
     * Gets called when a player leaves the server and the game.
     */
    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        gameController!!.getGameFromPlayer(event.player)?.leave(event.player)
    }


    /**
     * Gets called when the foodLevel changes and cancels it if the player is inside of a game.
     */
    @EventHandler
    fun onPlayerHungerEvent(event: FoodLevelChangeEvent) {
        val game = gameController!!.getGameFromPlayer(event.entity as Player)
        if (game != null) {
            event.isCancelled = true
        }
    }

    /**
     * Gets called when the player interacts with his inventory and cancels it.
     */
    @EventHandler
    fun onPlayerClickInventoryEvent(event: InventoryClickEvent) {
        val game = gameController!!.getGameFromPlayer(event.whoClicked as Player)
        if (game != null) {
            event.isCancelled = true
            event.whoClicked.closeInventory()
        }
    }

    /**
     * Gets called when a player opens his inventory and cancels the action.
     */
    @EventHandler
    fun onPlayerOpenInventoryEvent(event: InventoryOpenEvent) {
        val game = gameController!!.getGameFromPlayer(event.player as Player)
        if (game != null) {
            event.isCancelled = true
        }
    }

    /**
     * Cancels all fall damage in the games.
     */
    @EventHandler
    fun onPlayerDamageEvent(event: EntityDamageEvent) {
        if (event.entity !is Player)
            return
        val player = event.entity as Player
        val game = gameController!!.getGameFromPlayer(player)
        if (game != null && event.cause == EntityDamageEvent.DamageCause.FALL) {
            event.isCancelled = true
        }
        if (game != null && event.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK && !game.arena.meta.customizingMeta.damageEnabled) {
            event.isCancelled = true
        }
    }

    /**
     * Caches the last interacting entity with the ball.
     */
    @EventHandler
    fun onBallInteractEvent(event: BallInteractEvent) {
        val game = gameController!!.games.find { p -> p.ball != null && p.ball!! == event.ball }
        if (game != null && game is SoccerGame) {
            game.lastInteractedEntity = event.entity
        }
    }
}