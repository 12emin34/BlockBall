package com.github.shynixn.blockball.bukkit.logic.business.controller

import com.github.shynixn.blockball.api.bukkit.business.controller.BukkitGameController
import com.github.shynixn.blockball.api.bukkit.business.entity.BukkitGame
import com.github.shynixn.blockball.api.bukkit.persistence.entity.BukkitArena
import com.github.shynixn.blockball.api.business.enumeration.GameType
import com.github.shynixn.blockball.bukkit.logic.business.commandexecutor.*
import com.github.shynixn.blockball.bukkit.logic.business.entity.game.BungeeCordMinigame
import com.github.shynixn.blockball.bukkit.logic.business.entity.game.HubGame
import com.github.shynixn.blockball.bukkit.logic.business.entity.game.LowLevelGame
import com.github.shynixn.blockball.bukkit.logic.business.entity.game.Minigame
import com.github.shynixn.blockball.bukkit.logic.business.listener.*
import com.github.shynixn.blockball.bukkit.logic.persistence.controller.ArenaRepository
import com.google.inject.Inject
import com.google.inject.Singleton
import com.sk89q.worldedit.WorldEdit.logger
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
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
@Singleton
class GameRepository : BukkitGameController, Runnable {

    /** ArenaController. */
    @Inject
    override lateinit var arenaController: ArenaRepository

    @Inject
    private lateinit var plugin: Plugin

    @Inject
    private lateinit var arenaCommandExecutor: NewArenaCommandExecutor

    @Inject
    private lateinit var reloadCommandExecutor: ReloadCommandExecutor

    @Inject
    private lateinit var joinCommandExecutor: JoinCommandExecutor

    @Inject
    private lateinit var leaveCommandExecutor: LeaveCommandExecutor

    @Inject
    private lateinit var stopCommandExecutor: StopCommandExecutor

    @Inject
    private lateinit var spectateCommandExecutor: SpectateCommandExecutor

    @Inject
    private lateinit var gameListener: GameListener

    @Inject
    private lateinit var doubleJumpListener: DoubleJumpListener

    @Inject
    private lateinit var hubGameListener: HubGameListener

    @Inject
    private lateinit var statsListener: StatsListener

    @Inject
    private lateinit var minigameListener: MinigameListener

    @Inject
    private lateinit var bungeeCordGameListener: BungeeCordGameListener

    /** Games. */
    val games: ArrayList<BukkitGame> = ArrayList()
    private var task: BukkitTask? = null

    /**
     * The general contract of the method `run` is that it may
     * take any action whatsoever.
     *
     * @see java.lang.Thread.run
     */
    override fun run() {
        games.toArray().forEach { p ->
            if ((p as LowLevelGame).closed) {
                games.remove(p)
                this.addGameForArena(p.arena)
            } else {
                p.run()
            }
        }
    }

    /** Removes an item from the repository. */
    override fun remove(item: BukkitGame) {
        if (this.games.contains(item)) {
            this.games.remove(item)
        }
    }

    /** Returns all items from the repository. */
    override fun getAll(): List<BukkitGame> {
        return games
    }

    /** Stores a new item into the repository. */
    override fun store(item: BukkitGame) {
        if (!this.games.contains(item)) {
            this.games.add(item)
        }
    }

    /** Returns the amount of items in the repository. */
    override val count: Int
        get() = games.size

    /** Returns the game with the given arena name. */
    override fun getGameFromArenaName(name: String): BukkitGame? {
        games.forEach { p ->
            if (p.arena.name.equals(name, true)) {
                return p
            }
        }
        return null
    }

    /** Returns the game with the given [player] spectating in it. */
    fun getGameFromSpectatingPlayer(player: Player): BukkitGame? {
        games.forEach { g ->
            if (g is Minigame) {
                if (g.spectators.containsKey(player)) {
                    return g
                }
            }
        }
        return null
    }

    /** Returns the game with the [player] inside. */
    override fun getGameFromPlayer(player: Player): BukkitGame? {
        games.forEach { p ->
            if (p.hasJoined(player)) {
                return p
            }
        }
        return null
    }

    /** Returns the game with the given arena displayName. */
    override fun getGameFromArenaDisplayName(name: String): BukkitGame? {
        games.forEach { p ->
            if (p.arena.displayName.equals(name, true)) {
                return p
            }
        }
        return null
    }

    /** Reloads the contents in the cache of the controller. */
    override fun reload() {
        if (task == null) {
            task = plugin.server.scheduler.runTaskTimer(plugin, this, 0L, 1L)
        }
        this.arenaController.reload()
        for (game in this.games) {
            try {
                game.close()
            } catch (e: Exception) {
                logger.log(Level.WARNING, "Failed to dispose game.", e)
            }
        }
        this.games.clear()
        this.arenaController.getAll().forEach { p ->
            addGameForArena(p)
        }
    }

    private fun addGameForArena(arena: BukkitArena) {
        when {
            arena.gameType == GameType.HUBGAME -> this.store(HubGame(arena))
            arena.gameType == GameType.MINIGAME -> this.store(Minigame(arena))
            arena.gameType == GameType.BUNGEE -> {
                this.store(BungeeCordMinigame(arena))
                plugin.logger.log(Level.INFO, "Server is now fully managed " +
                        "by BlockBall and available for joining game '" + arena.displayName + "'.")
                return
            }
        }
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * `try`-with-resources statement.
     * However, implementers of this interface are strongly encouraged
     * to make their `close` methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    override fun close() {
        games.forEach { p ->
            p.close()
        }
        games.clear()
    }
}