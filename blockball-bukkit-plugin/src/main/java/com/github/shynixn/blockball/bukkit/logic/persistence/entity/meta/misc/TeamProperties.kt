package com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta.misc

import com.github.shynixn.ball.bukkit.logic.persistence.configuration.Config
import com.github.shynixn.blockball.api.bukkit.event.entity.BukkitArena
import com.github.shynixn.blockball.api.persistence.entity.AreaSelection
import com.github.shynixn.blockball.api.persistence.entity.IPosition
import com.github.shynixn.blockball.api.persistence.entity.meta.misc.TeamMeta
import com.github.shynixn.blockball.bukkit.logic.business.helper.YamlSerializer
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta.BlockBallMetaCollection
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta.area.SelectedArea
import org.bukkit.Location
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
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
class TeamProperties(
        /** DisplayName of the team which gets used in the placeholder <red> or <blue>. */
        @YamlSerializer.YamlSerialize(orderNumber = 1, value = "displayname")
        override var displayName: String,
        /** Prefix of the team which gets used in the placeholder <redcolor> or <bluecolor>. */
        @YamlSerializer.YamlSerialize(orderNumber = 2, value = "prefix")
        override var prefix: String,
        /** Title of the message getting played when a player scores a goal. */
        @YamlSerializer.YamlSerialize(orderNumber = 11, value = "score-message-title")
        override var scoreMessageTitle: String,
        /** Subtitle of the message getting played when a player scores a goal. */
        @YamlSerializer.YamlSerialize(orderNumber = 12, value = "score-message-subtitle")
        override var scoreMessageSubTitle: String,
        /** Title of the message getting played when this team wins a match. */
        @YamlSerializer.YamlSerialize(orderNumber = 13, value = "win-message-title")
        override var winMessageTitle: String,
        /** Subtitle of the message getting played when this team wins a match. */
        @YamlSerializer.YamlSerialize(orderNumber = 14, value = "win-message-subtitle")
        override var winMessageSubTitle: String) : TeamMeta<Location, AreaSelection<Location, BukkitArena>, ItemStack> {

    /** Min amount of players in this team to start the match for this team. */
    @YamlSerializer.YamlSerialize(orderNumber = 3, value = "min-amount")
    override var minAmount: Int = 1
    /** Max amount of players in this team to start the match for this team. */
    @YamlSerializer.YamlSerialize(orderNumber = 4, value = "max-amount")
    override var maxAmount: Int = 10
    /** Spawnpoint of the team inside of the arena. */
    @YamlSerializer.YamlSerialize(orderNumber = 6, value = "spawnpoint")
    override var spawnpoint: IPosition? = null
    /** Goal properties of the team. */
    @YamlSerializer.YamlSerialize(orderNumber = 7, value = "goal")
    override val goal: AreaSelection<Location, AreaSelection<Location,BukkitArena>> = SelectedArea()
    /** Walkingspeed of the players in this team. */
    @YamlSerializer.YamlSerialize(orderNumber = 5, value = "walking-speed")
    override var walkingSpeed: Double = 0.2
    /** Message getting played when a player joins a match.*/
    @YamlSerializer.YamlSerialize(orderNumber = 9, value = "join-message")
    override var joinMessage: String = "You have joined the game."
    /** Message getting played when a player leave a match.*/
    @YamlSerializer.YamlSerialize(orderNumber = 10, value = "leave-message")
    override var leaveMessage: String = "You have left the game."
    /** Armor wearing this team. */
    override var armorContents: Array<ItemStack?>
        get() {
            val itemStacks = arrayOfNulls<ItemStack>(4)
            val configuration = YamlConfiguration()
            itemStacks.indices
                    .filter { this.internalArmorContents[it] != null }
                    .forEach {
                        try {
                            configuration.loadFromString(this.internalArmorContents[it])
                            itemStacks[it] = configuration.getItemStack("item")
                        } catch (e: InvalidConfigurationException) {
                            Config.Logger!!.log(Level.WARNING, "Failed to deserialize armor.", e)
                        }
                    }
            return itemStacks
        }
        set(value) {
            this.internalArmorContents = arrayOfNulls(4)
            for (i in value.indices) {
                if (value[i] != null) {
                    val configuration = YamlConfiguration()
                    configuration.set("item", value[i])
                    this.internalArmorContents[i] = configuration.saveToString()
                }
            }
        }

    @YamlSerializer.YamlSerialize(orderNumber = 8, value = "armor")
    private var internalArmorContents: Array<String?> = arrayOfNulls(4)
}