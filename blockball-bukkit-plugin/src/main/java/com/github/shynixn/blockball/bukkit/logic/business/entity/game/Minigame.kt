package com.github.shynixn.blockball.bukkit.logic.business.entity.game

import com.github.shynixn.ball.bukkit.core.logic.persistence.entity.SoundBuilder
import com.github.shynixn.blockball.api.bukkit.business.event.GameJoinEvent
import com.github.shynixn.blockball.api.bukkit.business.event.GameLeaveEvent
import com.github.shynixn.blockball.api.bukkit.business.event.GameWinEvent
import com.github.shynixn.blockball.api.bukkit.business.event.GoalShootEvent
import com.github.shynixn.blockball.api.bukkit.persistence.entity.BukkitArena
import com.github.shynixn.blockball.api.business.enumeration.GameStatus
import com.github.shynixn.blockball.api.business.enumeration.GameType
import com.github.shynixn.blockball.api.business.enumeration.Permission
import com.github.shynixn.blockball.api.business.enumeration.Team
import com.github.shynixn.blockball.api.persistence.entity.meta.misc.TeamMeta
import com.github.shynixn.blockball.bukkit.BlockBallPlugin
import com.github.shynixn.blockball.bukkit.logic.business.entity.container.PlayerStorage
import com.github.shynixn.blockball.bukkit.logic.business.helper.*
import com.github.shynixn.blockball.bukkit.logic.persistence.configuration.Config
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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
open class Minigame(arena: BukkitArena) : SoccerGame(arena) {

    private val blingsound = SoundBuilder("NOTE_PLING", 1.0, 2.0)
    protected var isLobbyCountdownRunning: Boolean = false
    private var lobbyCountdown: Int = 0
    protected var isGameRunning: Boolean = false
    var gameCountdown: Int = 0
    private var isEndGameRunning: Boolean = false
    var spectators = HashMap<Player, PlayerStorage>()

    /**
     * Gets called when a player scores a point for the given team.
     */
    override fun onScore(team: Team, teamMeta: TeamMeta<Location, ItemStack>) {
        val scoreMessageTitle = teamMeta.scoreMessageTitle
        val scoreMessageSubTitle = teamMeta.scoreMessageSubTitle

        val players = ArrayList(getPlayers())
        val additionalPlayers = getAdditionalNotificationPlayers()
        players.addAll(additionalPlayers.filter { pair -> pair.second }.map { p -> p.first })

        players.forEach { p -> p.sendScreenMessage(scoreMessageTitle, scoreMessageSubTitle, this) }

        if (lastInteractedEntity != null && lastInteractedEntity is Player) {
            val event = GoalShootEvent(this, lastInteractedEntity as Player, team)
            Bukkit.getServer().pluginManager.callEvent(event)
        }
    }

    /**
     * Gets called when the match ends in a draw.
     */
    private fun onDraw() {
        val additionalPlayers = getAdditionalNotificationPlayers().filter { pair -> pair.second }.map { p -> p.first }
        additionalPlayers.forEach { p -> p.sendScreenMessage(arena.meta.redTeamMeta.drawMessageTitle, arena.meta.redTeamMeta.drawMessageSubTitle, this) }

        this.redTeam.forEach { p -> p.sendScreenMessage(arena.meta.redTeamMeta.drawMessageTitle, arena.meta.redTeamMeta.drawMessageSubTitle, this) }
        this.blueTeam.forEach { p -> p.sendScreenMessage(arena.meta.blueTeamMeta.drawMessageTitle, arena.meta.blueTeamMeta.drawMessageSubTitle, this) }
    }

    /**
     * Gets called when a team wins the game.
     */
    override fun onWin(team: Team, teamMeta: TeamMeta<Location, ItemStack>) {
        val winMessageTitle = teamMeta.winMessageTitle
        val winMessageSubTitle = teamMeta.winMessageSubTitle

        val players = ArrayList(getPlayers())
        val additionalPlayers = getAdditionalNotificationPlayers()
        players.addAll(additionalPlayers.filter { pair -> pair.second }.map { p -> p.first })

        players.forEach { p -> p.sendScreenMessage(winMessageTitle, winMessageSubTitle, this) }

        val event = GameWinEvent(this, team)
        Bukkit.getServer().pluginManager.callEvent(event)

        setEndGame()
    }

    /** Leave the game. */
    override fun leave(player: Player) {
        if (spectators.contains(player)) {
            spectators[player]!!.resetState()
            player.teleport(arena.meta.lobbyMeta.leaveSpawnpoint!!.toBukkitLocation())
            spectators.remove(player)
        }

        super.leave(player)

        if (!ingameStats.containsKey(player)) {
            return
        }

        val event = GameLeaveEvent(this, player)
        Bukkit.getServer().pluginManager.callEvent(event)
    }

    /**
     * Spectates the game.
     */
    fun spectate(player: Player) {
        if (spectators.contains(player)) {
            return
        }

        if (!isAllowedToSpectateWithPermissions(player)) {
            return
        }

        if (arena.meta.spectatorMeta.spectateSpawnpoint != null) {
            player.teleport(arena.meta.spectatorMeta.spectateSpawnpoint!!.toBukkitLocation())

        } else {
            player.teleport(arena.meta.ballMeta.spawnpoint!!.toBukkitLocation())
        }

        spectators[player] = PlayerStorage(player)
        spectators[player]!!.storeForType(GameType.MINIGAME)

        player.gameMode = GameMode.SPECTATOR
    }

    /** Join the game. */
    override fun join(player: Player, team: Team?): Boolean {
        if (isGameRunning || isEndGameRunning || isLobbyFull()) {
            ChatBuilder().text(Config.prefix + arena.meta.spectatorMeta.spectateStartMessage[0].replaceGamePlaceholder(this))
                    .nextLine()
                    .component(Config.prefix + arena.meta.spectatorMeta.spectateStartMessage[1].replaceGamePlaceholder(this))
                    .setClickAction(ChatBuilder.ClickAction.RUN_COMMAND
                            , "/" + plugin.config.getString("global-spectate.command") + " " + arena.name)
                    .setHoverText(" ")
                    .builder().sendMessage(player)

            return false
        }

        if (!this.isAllowedToJoinWithPermissions(player)) {
            return false
        }

        if (this.hasJoined(player) && team != null) {
            var targetTeam = team
            val amount = getAmountOfQueuedPlayersInThisTeam(targetTeam)

            if (this.arena.meta.lobbyMeta.onlyAllowEventTeams) {
                val blueTeamAmount = getAmountOfQueuedPlayersInThisTeam(Team.BLUE)
                val redTeamAmount = getAmountOfQueuedPlayersInThisTeam(Team.RED)

                if (blueTeamAmount > redTeamAmount) {
                    targetTeam = Team.RED
                } else if (blueTeamAmount < redTeamAmount) {
                    targetTeam = Team.BLUE
                }
            }

            if (targetTeam == Team.RED) {
                if (amount >= this.arena.meta.redTeamMeta.maxAmount) {
                    return false
                }

                joinTeam(player, arena.meta.redTeamMeta, redTeam)
            } else if (targetTeam == Team.BLUE) {
                if (amount >= this.arena.meta.blueTeamMeta.maxAmount) {
                    return false
                }

                joinTeam(player, arena.meta.blueTeamMeta, blueTeam)
            }

            ingameStats[player]!!.team = targetTeam
            return true
        }

        this.leave(player)
        this.prepareLobbyStatsForPlayer(player)

        if (team != null) {
            join(player, team)
        }

        return true
    }

    /**
     * Returns the amount of queues players.
     */
    private fun getAmountOfQueuedPlayersInThisTeam(team: Team): Int {
        var amount = 0

        ingameStats.values.forEach { p ->
            if (p.team != null && p.team == team) {
                amount++
            }
        }

        return amount
    }

    /**
     * Thread save method to listen on the second tick cycle of the game.
     */
    override fun onTwentyTicks() {
        if (this.arena.gameType != GameType.MINIGAME) {
            this.close()
            return
        }

        if (isEndGameRunning) {
            if (ball != null) {
                ball!!.remove()
                ball = null
            }

            gameCountdown--

            this.ingameStats.keys.toTypedArray().forEach { p ->
                if (gameCountdown <= 10) {
                    p.exp = (gameCountdown.toFloat() / 10.0F)
                }
                p.level = gameCountdown
            }

            if (gameCountdown <= 0) {
                close()
            }

            return
        }

        if (isLobbyCountdownRunning) {

            if (lobbyCountdown > 10) {
                val amountPlayers = this.arena.meta.blueTeamMeta.maxAmount + this.arena.meta.redTeamMeta.maxAmount

                if (this.ingameStats.size >= amountPlayers) {
                    lobbyCountdown = 10
                }
            }

            lobbyCountdown--

            this.ingameStats.keys.toTypedArray().forEach { p ->
                if (lobbyCountdown <= 10) {
                    p.exp = 1.0F - (lobbyCountdown.toFloat() / 10.0F)
                }

                p.level = lobbyCountdown
            }

            if (lobbyCountdown < 5) {
                this.playBlingSound()
            }

            if (lobbyCountdown <= 0) {
                this.ingameStats.keys.toTypedArray().forEach { p ->
                    if (lobbyCountdown <= 10) {
                        p.exp = 1.0F
                    }

                    p.level = 0
                }

                gameCountdown = this.arena.meta.minigameMeta.matchDuration
                isLobbyCountdownRunning = false
                isGameRunning = true
                startGame()
            }
        }

        if (!isLobbyCountdownRunning) {

            if (canStartLobbyCountdown()) {
                isLobbyCountdownRunning = true
                lobbyCountdown = arena.meta.minigameMeta.lobbyDuration
            } else if (!isGameRunning) {
                ingameStats.keys.toTypedArray().sendActionBarMessage(arena.meta.minigameMeta.playersRequiredToStartMessage.replaceGamePlaceholder(this))
            }
        }

        if (isGameRunning) {
            gameCountdown--

            this.ingameStats.keys.toTypedArray().forEach { p ->
                if (gameCountdown <= 10) {
                    p.exp = (gameCountdown.toFloat() / 10.0F)
                }
                p.level = gameCountdown
            }

            if (gameCountdown <= 0) {
                setEndGame()
                timesUpGame()
            }

            if (this.ingameStats.isEmpty()) {
                this.close()
            }
        }
    }

    /**
     * Gets called when the game ends.
     */
    private fun timesUpGame() {
        when {
            this.redGoals == this.blueGoals -> {
                this.onMatchEnd(null, null)
                this.onDraw()
            }
            this.redGoals > this.blueGoals -> {
                this.onMatchEnd(this.redTeam, this.blueTeam)
                this.onWin(Team.RED, this.arena.meta.redTeamMeta)
            }
            else -> {
                this.onMatchEnd(this.blueTeam, this.redTeam)
                this.onWin(Team.BLUE, this.arena.meta.blueTeamMeta)
            }
        }
    }

    /**
     * Starts the game.
     */
    private fun startGame() {
        status = GameStatus.RUNNING
        ingameStats.keys.toTypedArray().forEach { p ->

            val event = GameJoinEvent(this, p)
            Bukkit.getServer().pluginManager.callEvent(event)

            val stats = ingameStats[p]

            if (stats!!.team == null) {
                if (redTeam.size < blueTeam.size) {
                    stats.team = Team.RED
                    joinTeam(p, arena.meta.redTeamMeta, redTeam)
                } else {
                    stats.team = Team.BLUE
                    joinTeam(p, arena.meta.blueTeamMeta, blueTeam)
                }
            }

            if (stats.team == Team.RED) {
                val teamMeta = arena.meta.redTeamMeta

                if (teamMeta.spawnpoint == null) {
                    p.teleport(arena.meta.ballMeta.spawnpoint!!.toBukkitLocation())
                } else {
                    p.teleport(teamMeta.spawnpoint!!.toBukkitLocation())
                }
            } else {
                val teamMeta = arena.meta.blueTeamMeta

                if (teamMeta.spawnpoint == null) {
                    p.teleport(arena.meta.ballMeta.spawnpoint!!.toBukkitLocation())
                } else {
                    p.teleport(teamMeta.spawnpoint!!.toBukkitLocation())
                }
            }
        }
    }

    /**
     * Joins the [player] to the given [teamMeta] and [teamPlayers].
     */
    private fun joinTeam(player: Player, teamMeta: TeamMeta<Location, ItemStack>, teamPlayers: MutableList<Player>) {
        if (teamPlayers.contains(player)) {
            return
        }

        if (ingameStats[player]!!.team != null) {
            if (ingameStats[player]!!.team == Team.RED) {
                redTeam.remove(player)
            } else {
                blueTeam.remove(player)
            }
        }

        teamPlayers.add(player)
        player.walkSpeed = teamMeta.walkingSpeed.toFloat()
        player.inventory.contents = teamMeta.inventoryContents.clone()
        player.inventory.armorContents = teamMeta.armorContents.clone()
        player.updateInventory()
        player.sendMessage(Config.prefix + teamMeta.joinMessage.replaceGamePlaceholder(this, teamMeta, teamPlayers))
    }

    /**
     * Returns if the lobby is full.
     * @return isFull
     */
    private fun isLobbyFull(): Boolean {
        val amount = arena.meta.redTeamMeta.maxAmount + arena.meta.blueTeamMeta.maxAmount

        if (this.ingameStats.size >= amount) {
            return true
        }

        return false
    }

    /**
     * Returns if the lobby countdown can already be started.
     * @return canStart
     */
    private fun canStartLobbyCountdown(): Boolean {
        val amount = arena.meta.redTeamMeta.minAmount + arena.meta.blueTeamMeta.minAmount

        if (!isGameRunning && this.ingameStats.size >= amount && ingameStats.isNotEmpty()) {
            return true
        }

        return false
    }

    /**
     * Plays the fancy bling countdown sound.
     */
    private fun playBlingSound() {
        try {
            this.blingsound.apply(this.ingameStats.keys)
        } catch (e: Exception) {
            Bukkit.getServer().consoleSender.sendMessage(BlockBallPlugin.PREFIX_CONSOLE + ChatColor.RED + "Invalid 1.8/1.9 sound. [BlingSound]")
        }
    }

    /**
     * Sets the game ending with 10 seconds cooldown.
     */
    private fun setEndGame() {
        if (!isEndGameRunning) {
            gameCountdown = 10
        }

        isGameRunning = false
        isEndGameRunning = true
        blockBallSpawning = true
    }

    /**
     * Returns a list of players which can be also notified
     */
    override fun getAdditionalNotificationPlayers(): MutableList<Pair<Player, Boolean>> {
        val players = super.getAdditionalNotificationPlayers()
        players.addAll(spectators.keys.map { p -> Pair(p, true) })
        return players
    }

    /**
     * Returns if the given [player] is allowed to join the match.
     */
    private fun isAllowedToSpectateWithPermissions(player: Player): Boolean {
        if (player.hasPermission(Permission.SPECTATE.permission + ".all")
                || player.hasPermission(Permission.SPECTATE.permission + "." + this.arena.name)) {
            return true
        }

        player.sendMessage(Config.prefix + Config.spectateGamePermissionmessage)

        return false
    }


    /**
     * Prepares the lobby stat for the given [player].
     */
    private fun prepareLobbyStatsForPlayer(player: Player) {
        val stats = PlayerStorage(player)
        stats.storeForType(GameType.MINIGAME)

        player.allowFlight = false
        player.isFlying = false
        player.maxHealth = 20.0
        player.health = 20.0
        player.foodLevel = 20
        player.level = 0
        player.exp = 0.0F
        player.gameMode = arena.meta.lobbyMeta.gamemode as GameMode

        player.inventory.armorContents = arrayOfNulls(4)
        player.inventory.clear()

        player.updateInventory()
        player.teleport(arena.meta.minigameMeta.lobbySpawnpoint!!.toBukkitLocation())

        this.ingameStats[player] = stats
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * `try`-with-resources statement.
     * @throws Exception if this resource cannot be closed
     */
    override fun close() {
        spectators.keys.toTypedArray().forEach { s ->
            leave(s)
        }

        super.close()
    }
}