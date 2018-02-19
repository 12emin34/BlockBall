package com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta

import com.github.shynixn.blockball.api.persistence.entity.meta.ArenaMeta
import com.github.shynixn.blockball.api.persistence.entity.meta.display.HologramMeta
import com.github.shynixn.blockball.api.persistence.entity.meta.misc.ArenaProtectionMeta
import com.github.shynixn.blockball.api.persistence.entity.meta.misc.CustomizationMeta
import com.github.shynixn.blockball.api.persistence.entity.meta.misc.RewardMeta
import com.github.shynixn.blockball.bukkit.logic.business.helper.YamlSerializer
import com.github.shynixn.blockball.bukkit.logic.business.helper.setColor
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.BallData
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta.display.BossBarBuilder
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta.display.HologramBuilder
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta.display.ScoreboardBuilder
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta.lobby.BungeeCordLobbyProperties
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta.lobby.HubLobbyProperties
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta.lobby.LobbyProperties
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta.lobby.MinigameLobbyProperties
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta.misc.*
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

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
class BlockBallMetaCollection : ArenaMeta<Location, ItemStack, Vector, Player, Material> {
    /** Meta data of the customizing Properties. */
    @YamlSerializer.YamlSerialize(orderNumber = 12, value = "customizing-meta")
    override val customizingMeta: CustomizationProperties = CustomizationProperties()
    /** Meta data for rewards */
    @YamlSerializer.YamlSerialize(orderNumber = 10, value = "reward-meta")
    override val rewardMeta: RewardProperties = RewardProperties()
    /** Meta data of all holograms. */
    override val hologramMetas: ArrayList<HologramMeta>
        get() = this.internalHologramMetas as ArrayList<HologramMeta>
    /** Meta data of a generic lobby. */
    @YamlSerializer.YamlSerialize(orderNumber = 1, value = "meta")
    override val lobbyMeta: LobbyProperties = LobbyProperties()
    /** Meta data of the hub lobby. */
    @YamlSerializer.YamlSerialize(orderNumber = 2, value = "hubgame-meta")
    override var hubLobbyMeta: HubLobbyProperties = HubLobbyProperties()
    /** Meta data of the minigame lobby. */
    @YamlSerializer.YamlSerialize(orderNumber = 3, value = "minigame-meta")
    override val minigameMeta: MinigameLobbyProperties = MinigameLobbyProperties()
    /** Meta data of the bungeecord lobby. */
    @YamlSerializer.YamlSerialize(orderNumber = 4, value = "bungeecord-meta")
    override val bungeeCordMeta: BungeeCordLobbyProperties = BungeeCordLobbyProperties()
    /** Meta data of the doubleJump. */
    @YamlSerializer.YamlSerialize(orderNumber = 8, value = "double-jump")
    override val doubleJumpMeta: DoubleJumpProperties = DoubleJumpProperties()
    /** Meta data of the bossbar. */
    @YamlSerializer.YamlSerialize(orderNumber = 7, value = "bossbar")
    override val bossBarMeta: BossBarBuilder = BossBarBuilder()
    /** Meta data of the scoreboard. */
    @YamlSerializer.YamlSerialize(orderNumber = 6, value = "scoreboard")
    override val scoreboardMeta: ScoreboardBuilder = ScoreboardBuilder()
    /** Meta data of proection. */
    @YamlSerializer.YamlSerialize(orderNumber = 5, value = "protection")
    override val protectionMeta: ArenaProtectionMeta<Vector> = ArenaProtectionData()
    /** Meta data of the ball. */
    @YamlSerializer.YamlSerialize(orderNumber = 4, value = "ball", classicSerialize = YamlSerializer.ManualSerialization.CONSTRUCTOR)
    override val ballMeta: BallData = BallData("http://textures.minecraft.net/texture/8e4a70b7bbcd7a8c322d522520491a27ea6b83d60ecf961d2b4efbbf9f605d")
    /** Meta data of the blueTeam. */
    @YamlSerializer.YamlSerialize(orderNumber = 3, value = "team-blue")
    override val blueTeamMeta: TeamProperties = TeamProperties("Team Blue", "&9", "<bluecolor><bluescore> : <redcolor><redscore>", "<bluecolor><player> scored for <blue>", "<bluecolor><blue>", "<blue>&a has won the match", "<bluecolor><blue>", "&eMatch ended in a draw.")
    /** Meta data of the redTeam. */
    @YamlSerializer.YamlSerialize(orderNumber = 2, value = "team-red")
    override val redTeamMeta: TeamProperties = TeamProperties("Team Red", "&c", "<redcolor><redscore> : <bluecolor><bluescore>", "<redcolor><player> scored for <red>", "<redcolor><red>", "<red>&a has won the match", "<redcolor><red>", "&eMatch ended in a draw.")

    @YamlSerializer.YamlSerialize(orderNumber = 9, value = "holograms")
    private val internalHologramMetas: ArrayList<HologramBuilder> = ArrayList()

    init {
        redTeamMeta.armorContents = arrayOf(ItemStack(Material.LEATHER_BOOTS).setColor(Color.RED)
                , ItemStack(Material.LEATHER_LEGGINGS).setColor(Color.RED), ItemStack(Material.LEATHER_CHESTPLATE).setColor(Color.RED), null)
        blueTeamMeta.armorContents = arrayOf(ItemStack(Material.LEATHER_BOOTS).setColor(Color.BLUE)
                , ItemStack(Material.LEATHER_LEGGINGS).setColor(Color.BLUE), ItemStack(Material.LEATHER_CHESTPLATE).setColor(Color.BLUE), null)

        ballMeta.isAlwaysBounceBack = true
        ballMeta.isCarryable = false
        ballMeta.hitBoxSize = 3.0
        ballMeta.modifiers.verticalKickStrengthModifier = 1.5
        ballMeta.modifiers.horizontalKickStrengthModifier = 1.5
    }
}