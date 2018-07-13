package com.github.shynixn.blockball.bukkit.logic.business.service

import com.github.shynixn.blockball.api.bukkit.persistence.entity.BukkitArena
import com.github.shynixn.blockball.api.business.entity.VirtualArmorstand
import com.github.shynixn.blockball.api.business.service.VirtualArenaService
import com.github.shynixn.blockball.api.persistence.entity.Arena
import com.github.shynixn.blockball.api.persistence.entity.basic.StorageLocation
import com.github.shynixn.blockball.bukkit.nms.NMSRegistry
import com.google.inject.Inject
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
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
class VirtualArenaServiceImpl @Inject constructor(private val plugin: Plugin) : VirtualArenaService {

    /**
     * Displays the [arena] virtual locations for the given [player] for the given amount of [seconds].
     */
    override fun <Player> displayForPlayer(player: Player, arena: Arena<*, *, *, *, *>, seconds: Int) {
        if (player !is org.bukkit.entity.Player) {
            throw IllegalArgumentException("Player has to be a bukkit player!")
        }

        if (arena !is BukkitArena) {
            throw IllegalArgumentException("Arena has to be a bukkit arena!")
        }

        plugin.server.scheduler.runTaskAsynchronously(plugin, {
            val items = ArrayList<VirtualArmorstand>()

            if (arena.meta.redTeamMeta.goal.lowerCorner != null && arena.meta.redTeamMeta.goal.upperCorner != null) {
                displayArmorstands(player, items, arena.meta.redTeamMeta.goal.lowerCorner!!, arena.meta.redTeamMeta.goal.upperCorner!!)
            }
            if (arena.meta.blueTeamMeta.goal.lowerCorner != null && arena.meta.blueTeamMeta.goal.upperCorner != null) {
                displayArmorstands(player, items, arena.meta.blueTeamMeta.goal.lowerCorner!!, arena.meta.blueTeamMeta.goal.upperCorner!!)
            }

            plugin.server.scheduler.runTaskLaterAsynchronously(plugin, {
                items.forEach { i ->
                    i.close()
                }

                items.clear()
            }, 20 * 20)
        })
    }

    /**
     * Displays the armorstands between the given [lowCorner] and [upCorner] location for the given [player].
     */
    private fun displayArmorstands(player: Player, items: MutableList<VirtualArmorstand>, lowCorner: StorageLocation, upCorner: StorageLocation) {
        var j = lowCorner.y
        while (j <= upCorner.y) {
            var i = lowCorner.x
            while (i <= upCorner.x) {
                var k = lowCorner.z
                while (k <= upCorner.z) {
                    val armorstand = NMSRegistry.createVirtualArmorstand(player, Location(player.world, i, j, k), Material.STAINED_GLASS.id, 1)
                    armorstand.spawn()
                    items.add(armorstand)
                    k++
                }
                i++
            }
            j++
        }
    }
}