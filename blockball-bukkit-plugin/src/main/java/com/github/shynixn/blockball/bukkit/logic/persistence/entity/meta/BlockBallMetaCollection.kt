package com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta

import com.github.shynixn.blockball.api.bukkit.event.entity.BukkitArena
import com.github.shynixn.blockball.api.persistence.entity.AreaSelection
import com.github.shynixn.blockball.api.persistence.entity.Persistenceable
import com.github.shynixn.blockball.api.persistence.entity.meta.ArenaMeta
import com.github.shynixn.blockball.api.persistence.entity.meta.misc.TeamMeta
import com.github.shynixn.blockball.bukkit.logic.business.helper.YamlSerializer
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.PersistenceObject
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta.lobby.HubLobbyProperties
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta.misc.TeamProperties
import org.bukkit.Location
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
class BlockBallMetaCollection : ArenaMeta<Location, AreaSelection<Location, BukkitArena>, ItemStack> {
    /** Meta data of the blueTeam. */
    override val blueTeamMeta: TeamProperties
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    /** Meta data of the hublobby. */
    @YamlSerializer.YamlSerialize(orderNumber = 1, value = "hubgame")
    override var hubLobbyMeta: HubLobbyProperties = HubLobbyProperties()
    /** Meta data of the redTeam. */
    @YamlSerializer.YamlSerialize(orderNumber = 1, value = "team-red")
    override val redTeamMeta: TeamProperties
            = TeamProperties("Team Red", "&c", "<redcolor><player>", "scored for <red>.", "<redcolor><red>", "<redcolor> has won the match");
}