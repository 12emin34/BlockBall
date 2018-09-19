@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.blockball.bukkit.logic.business.service

import com.github.shynixn.blockball.api.bukkit.event.GameJoinEvent
import com.github.shynixn.blockball.api.bukkit.event.GameLeaveEvent
import com.github.shynixn.blockball.api.business.enumeration.*
import com.github.shynixn.blockball.api.business.service.*
import com.github.shynixn.blockball.api.persistence.entity.*
import com.github.shynixn.blockball.bukkit.logic.business.extension.isLocationInSelection
import com.github.shynixn.blockball.bukkit.logic.business.extension.replaceGamePlaceholder
import com.github.shynixn.blockball.bukkit.logic.business.extension.toBukkitMaterial
import com.github.shynixn.blockball.bukkit.logic.business.extension.toLocation
import com.github.shynixn.blockball.bukkit.logic.business.nms.VersionSupport
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.util.Vector
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
class GameActionServiceImpl<in G : Game> @Inject constructor(private val plugin: Plugin, private val gameHubGameActionService: GameHubGameActionService, private val bossBarService: BossBarService, private val configurationService: ConfigurationService, private val hubGameActionService: GameHubGameActionService, private val minigameActionService: GameMiniGameActionService<MiniGame>, private val bungeeCordGameActionService: GameBungeeCordGameActionService, private val scoreboardService: ScoreboardService, private val hologramService: HologramService, private val dependencyService: DependencyService, private val dependencyBossBarApiService: DependencyBossBarApiService, private val gameSoccerService: GameSoccerService<Game>) : GameActionService<G> {
    private val prefix = configurationService.findValue<String>("messages.prefix")
    private val signPostMaterial = MaterialType.SIGN_POST.toBukkitMaterial()

    /**
     * Lets the given [player] leave join the given [game]. Optional can the prefered
     * [team] be specified but the team can still change because of arena settings.
     * Does nothing if the player is already in a Game.
     */
    override fun <P> joinGame(game: G, player: P, team: Team?): Boolean {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        if (!isAllowedToJoinWithPermissions(game, player)) {
            return false
        }

        val event = GameJoinEvent(player, game)
        Bukkit.getServer().pluginManager.callEvent(event)

        if (event.isCancelled) {
            return false
        }

        if (game is HubGame) {
            return gameHubGameActionService.joinGame(game, player, team)
        }
        if (game is MiniGame) {
            return minigameActionService.joinGame(game, player, team)
        }

        throw RuntimeException("Game not supported!")
    }

    /**
     * Lets the given [player] leave the given [game].
     * Does nothing if the player is not in the game.
     */
    override fun <P> leaveGame(game: G, player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        val event = GameLeaveEvent(player, game)
        Bukkit.getServer().pluginManager.callEvent(event)

        if (event.isCancelled) {
            return
        }

        if (game.scoreboard != null && player.scoreboard == game.scoreboard) {
            player.scoreboard = Bukkit.getScoreboardManager().newScoreboard
        }

        if (game.bossBar != null) {
            bossBarService.removePlayer(game.bossBar, player)
        }

        game.holograms.forEach { h ->
            h.removeWatcher(player)
        }

        if (game is HubGame) {
            hubGameActionService.leaveGame(game, player)
        }
        if (game is MiniGame) {
            minigameActionService.leaveGame(game, player)
        }
        if (game is BungeeCordGame) {
            bungeeCordGameActionService.leaveGame(game, player)
        }

        if (game.ingamePlayersStorage.containsKey(player)) {
            val storage = game.ingamePlayersStorage[player]!!

            if (storage.team == Team.RED) {
                player.sendMessage(prefix + game.arena.meta.redTeamMeta.leaveMessage)
            } else if (storage.team == Team.BLUE) {
                player.sendMessage(prefix + game.arena.meta.blueTeamMeta.leaveMessage)
            }

            game.ingamePlayersStorage.remove(player)
        }

        if (game.arena.meta.lobbyMeta.leaveSpawnpoint != null) {
            player.teleport(game.arena.meta.lobbyMeta.leaveSpawnpoint!!.toLocation())
        }
    }


    /**
     * Closes the given game and all underlying resources.
     */
    override fun closeGame(game: G) {
        if (game.closed) {
            return
        }

        if (game is HubGame) {
            hubGameActionService.closeGame(game)
        }
        if (game is MiniGame) {
            minigameActionService.closeGame(game)
        }
        if (game is BungeeCordGame) {
            bungeeCordGameActionService.closeGame(game)
        }

        game.status = GameStatus.DISABLED
        game.closed = true
        game.ingamePlayersStorage.keys.toTypedArray().forEach { p -> leaveGame(game, p) }
        game.ingamePlayersStorage.clear()
        game.ball?.remove()
        game.doubleJumpCoolDownPlayers.clear()
        game.holograms.forEach { h -> h.remove() }
        game.holograms.clear()

        if (game.bossBar != null) {
            bossBarService.cleanResources(game.bossBar)
        }
    }

    /**
     * Handles the game actions per tick. [ticks] parameter shows the amount of ticks
     * 0 - 20 for each second.
     */
    override fun handle(game: G, ticks: Int) {
        if (!game.arena.enabled || game.closing) {
            game.status = GameStatus.DISABLED
            onUpdateSigns(game)
            closeGame(game)
            if (!game.ingamePlayersStorage.isEmpty() && game.arena.gameType != GameType.BUNGEE) {
                game.closing = true
            }
            return
        }

        if (game.status == GameStatus.DISABLED) {
            game.status = GameStatus.ENABLED
        }

        if (Bukkit.getWorld(game.arena.meta.ballMeta.spawnpoint!!.worldName) == null) {
            return
        }

        if (game is HubGame) {
            hubGameActionService.handle(game, ticks)
        }
        if (game is MiniGame) {
            minigameActionService.handle(game, ticks)
        }
        if (game is BungeeCordGame) {
            bungeeCordGameActionService.handle(game, ticks)
        }

        this.gameSoccerService.handle(game, ticks)

        if (ticks >= 20) {
            if (game.closing) {
                return
            }

            this.kickUnwantedEntitiesOutOfForcefield(game)
            this.onUpdateSigns(game)
            this.updateScoreboard(game)
            this.updateBossBar(game)
            this.updateDoubleJumpCooldown(game)
            this.updateHolograms(game)
            this.updateDoubleJumpCooldown(game)
        }
    }

    private fun onUpdateSigns(game: Game) {
        try {
            for (i in game.arena.meta.redTeamMeta.signs.indices) {
                val position = game.arena.meta.redTeamMeta.signs[i]
                if (!replaceTextOnSign(game, position, game.arena.meta.redTeamMeta.signLines, game.arena.meta.redTeamMeta)) {
                    game.arena.meta.redTeamMeta.signs.removeAt(i)
                    return
                }
            }
            for (i in game.arena.meta.blueTeamMeta.signs.indices) {
                val position = game.arena.meta.blueTeamMeta.signs[i]
                if (!replaceTextOnSign(game, position, game.arena.meta.blueTeamMeta.signLines, game.arena.meta.blueTeamMeta)) {
                    game.arena.meta.blueTeamMeta.signs.removeAt(i)
                    return
                }
            }
            for (i in game.arena.meta.lobbyMeta.joinSigns.indices) {
                val position = game.arena.meta.lobbyMeta.joinSigns[i]
                if (!replaceTextOnSign(game, position, game.arena.meta.lobbyMeta.joinSignLines, null)) {
                    game.arena.meta.lobbyMeta.joinSigns.removeAt(i)
                    return
                }
            }
            for (i in game.arena.meta.lobbyMeta.leaveSigns.indices) {
                val position = game.arena.meta.lobbyMeta.leaveSigns[i]
                if (!replaceTextOnSign(game, position, game.arena.meta.lobbyMeta.leaveSignLines, null)) {
                    game.arena.meta.lobbyMeta.leaveSigns.removeAt(i)
                    return
                }
            }
        } catch (e: Exception) { // Removing sign task could clash with updating signs.
            plugin.logger.log(Level.INFO, "Sign update was cached.", e)
        }
    }

    private fun replaceTextOnSign(game: Game, signPosition: Position, lines: List<String>, teamMeta: TeamMeta?): Boolean {
        var players = game.redTeam
        if (game.arena.meta.blueTeamMeta == teamMeta) {
            players = game.blueTeam
        }
        val location = signPosition.toLocation()
        if (location.block.type != signPostMaterial && location.block.type != Material.WALL_SIGN)
            return false
        val sign = location.block.state as Sign
        for (i in lines.indices) {
            val text = lines[i]
            sign.setLine(i, text.replaceGamePlaceholder(game, teamMeta, players as List<Player>))
        }
        sign.update(true)
        return true
    }

    private fun kickUnwantedEntitiesOutOfForcefield(game: G) {
        if (game.arena.meta.protectionMeta.entityProtectionEnabled) {
            game.arena.meta.ballMeta.spawnpoint!!.toLocation().world.entities.forEach { p ->
                if (p !is Player && p !is ArmorStand && p !is Item && p !is ItemFrame) {
                    if (game.arena.isLocationInSelection(p.location)) {
                        val vector = game.arena.meta.protectionMeta.entityProtection
                        p.location.direction = vector as Vector
                        p.velocity = vector
                    }
                }
            }
        }
    }

    private fun updateHolograms(game: G) {
        if (game.holograms.size != game.arena.meta.hologramMetas.size) {
            game.holograms.forEach { h -> h.remove() }
            game.holograms.clear()

            game.arena.meta.hologramMetas.forEach { meta ->
                val hologram = hologramService.createNewHologram(meta)
                game.holograms.add(hologram)
            }
        }

        game.holograms.forEachIndexed { i, holo ->
            val players = ArrayList(game.inTeamPlayers)
            val additionalPlayers = getAdditionalNotificationPlayers(game)
            players.addAll(additionalPlayers.filter { pair -> pair.second }.map { p -> p.first })

            players.forEach { p ->
                holo.addWatcher(p)
            }

            additionalPlayers.filter { p -> !p.second }.forEach { p ->
                holo.removeWatcher(p.first)
            }

            val lines = ArrayList(game.arena.meta.hologramMetas[i].lines)
            for (i in lines.indices) {
                lines[i] = lines[i].replaceGamePlaceholder(game)
            }

            holo.setLines(lines)
        }
    }

    private fun updateBossBar(game: G) {
        val meta = game.arena.meta.bossBarMeta
        if (VersionSupport.getServerVersion().isVersionSameOrGreaterThan(VersionSupport.VERSION_1_9_R1)) {
            if (game.bossBar == null && game.arena.meta.bossBarMeta.enabled) {
                game.bossBar = bossBarService.createNewBossBar<Any>(game.arena.meta.bossBarMeta)
            }
            if (game.bossBar != null) {
                bossBarService.changeConfiguration(game.bossBar, meta.message.replaceGamePlaceholder(game), meta, null)

                val players = ArrayList(game.inTeamPlayers)
                val additionalPlayers = getAdditionalNotificationPlayers(game)
                players.addAll(additionalPlayers.filter { pair -> pair.second }.map { p -> p.first })

                val bossbarPlayers = bossBarService.getPlayers<Any, Player>(game.bossBar!!)

                additionalPlayers.filter { p -> !p.second }.forEach { p ->
                    if (bossbarPlayers.contains(p.first)) {
                        bossBarService.removePlayer(game.bossBar!!, p.first)
                    }
                }

                players.forEach { p ->
                    bossBarService.addPlayer(game.bossBar, p)
                }
            }
        } else if (dependencyService.isInstalled(PluginDependency.BOSSBARAPI)) {
            if (game.arena.meta.bossBarMeta.enabled) {
                val percentage = meta.percentage

                val players = ArrayList(game.inTeamPlayers)
                val additionalPlayers = getAdditionalNotificationPlayers(game)
                players.addAll(additionalPlayers.filter { pair -> pair.second }.map { p -> p.first })

                additionalPlayers.filter { p -> !p.second }.forEach { p ->
                    dependencyBossBarApiService.removeBossbarMessage(p.first)
                }

                players.forEach { p ->
                    dependencyBossBarApiService.setBossbarMessage(p, meta.message.replaceGamePlaceholder(game), percentage)
                }
            }
        }
    }

    private fun updateDoubleJumpCooldown(game: G) {
        game.doubleJumpCoolDownPlayers.keys.toTypedArray().forEach { p ->
            var time = game.doubleJumpCoolDownPlayers[p]!!
            time -= 1
            if (time <= 0) {
                game.doubleJumpCoolDownPlayers.remove(p)
            } else {
                game.doubleJumpCoolDownPlayers[p] = time
            }
        }
    }

    /**
     * Updates the scoreboard for all players when enabled.
     */
    private fun updateScoreboard(game: G) {
        if (!game.arena.meta.scoreboardMeta.enabled) {
            return
        }

        if (game.scoreboard == null) {
            game.scoreboard = Bukkit.getScoreboardManager().newScoreboard

            scoreboardService.setConfiguration(game.scoreboard, DisplaySlot.SIDEBAR, game.arena.meta.scoreboardMeta.title)
        }

        val players = ArrayList(game.inTeamPlayers)
        val additionalPlayers = getAdditionalNotificationPlayers(game)
        players.addAll(additionalPlayers.filter { pair -> pair.second }.map { p -> p.first })

        additionalPlayers.filter { p -> !p.second }.forEach { p ->
            if (p.first.scoreboard == game.scoreboard) {
                p.first.scoreboard = Bukkit.getScoreboardManager().newScoreboard
            }
        }

        players.map { p -> p as Player }.forEach { p ->
            if (game.scoreboard != null) {
                if (p.scoreboard != game.scoreboard) {
                    p.scoreboard = game.scoreboard as Scoreboard
                }

                val lines = game.arena.meta.scoreboardMeta.lines

                var j = lines.size
                for (i in 0 until lines.size) {
                    val line = lines[i].replaceGamePlaceholder(game)
                    scoreboardService.setLine(game.scoreboard, j, line)
                    j--
                }
            }
        }
    }

    /**
     * Returns a list of players which can be also notified
     */
    private fun getAdditionalNotificationPlayers(game: Game): MutableList<Pair<Player, Boolean>> {
        if (!game.arena.meta.spectatorMeta.notifyNearbyPlayers) {
            return ArrayList()
        }

        val players = ArrayList<Pair<Player, Boolean>>()
        val center = game.arena.center.toLocation()

        center.world.players.forEach { p ->
            if (!game.ingamePlayersStorage.containsKey(p)) {
                if (p.location.distance(center) <= game.arena.meta.spectatorMeta.notificationRadius) {
                    players.add(Pair(p, true))
                } else {
                    players.add(Pair(p, false))
                }
            }
        }

        return players
    }

    /**
     * Returns if the given [player] is allowed to join the match.
     */
    private fun isAllowedToJoinWithPermissions(game: G, player: Player): Boolean {
        if (player.hasPermission(Permission.JOIN.permission + ".all")
                || player.hasPermission(Permission.JOIN.permission + "." + game.arena.name)) {
            return true
        }

        val prefix = configurationService.findValue<String>("messages.prefix")
        val joinGamePermissionMessage = configurationService.findValue<String>("messages.no-permission-join-game")

        player.sendMessage(prefix + joinGamePermissionMessage)

        return false
    }
}