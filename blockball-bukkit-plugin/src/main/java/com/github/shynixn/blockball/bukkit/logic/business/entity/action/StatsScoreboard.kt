package com.github.shynixn.blockball.bukkit.logic.business.entity.action

import com.github.shynixn.blockball.api.persistence.entity.Stats
import com.github.shynixn.blockball.bukkit.logic.business.configuration.Config
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot

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
class StatsScoreboard(val player: Player) : SimpleScoreboard() {

    init {
        this.setDefaultObjective(SimpleScoreboard.DUMMY_TYPE)
        this.setDefaultTitle(Config.getInstance().statsScoreboardTitle)
        this.setDefaultDisplaySlot(DisplaySlot.SIDEBAR)
        this.addPlayer(player)
    }

    /**
     * Updates the stats on the scoreboard.
     *
     * @param stats stats
     */
    fun updateStats(player: Player, stats: Stats) {
        val lines = Config.getInstance().statsScoreboardLines
        var i = 0
        var j = lines.size
        while (i < lines.size) {
            val line = lines[i]
                    .replace("<player>", player.name)
                    .replace("<winrate>", String.format("%.2f", stats.winRate))
                    .replace("<playedgames>", stats.amountOfGamesPlayed.toString())
                    .replace("<goalspergame>", String.format("%.2f", stats.goalsPerGame))
            this.setDefaultLine(j, line)
            i++
            j--
        }
    }
}