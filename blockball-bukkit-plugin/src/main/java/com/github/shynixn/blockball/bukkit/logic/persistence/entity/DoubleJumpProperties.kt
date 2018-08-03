package com.github.shynixn.blockball.bukkit.logic.persistence.entity

import com.github.shynixn.ball.api.persistence.effect.ParticleEffectMeta
import com.github.shynixn.ball.bukkit.core.logic.persistence.entity.ParticleEffectData
import com.github.shynixn.ball.bukkit.core.logic.persistence.entity.SoundBuilder
import com.github.shynixn.blockball.api.persistence.entity.DoubleJumpMeta
import com.github.shynixn.blockball.bukkit.logic.business.entity.action.YamlSerializer
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.PersistenceObject
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player

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
class DoubleJumpProperties : PersistenceObject(), DoubleJumpMeta<Location, Player, Material> {
    /** Is the effect enabled or disabled?*/
    @YamlSerializer.YamlSerialize(orderNumber = 1, value = "enabled")
    override var enabled: Boolean = true
    /** Cooldown between activating this effect.*/
    @YamlSerializer.YamlSerialize(orderNumber = 2, value = "cooldown")
    override var cooldown: Int = 2
    /** Vertical strength modifier.*/
    @YamlSerializer.YamlSerialize(orderNumber = 3, value = "vertical-strength")
    override var verticalStrength: Double = 1.0
    /** Horizontal strength modifier.*/
    @YamlSerializer.YamlSerialize(orderNumber = 4, value = "horizontal-strength")
    override var horizontalStrength: Double = 2.0
    /** ParticleEffect being played when activating this.*/
    @YamlSerializer.YamlSerialize(orderNumber = 5, value = "particle-effect", classicSerialize = YamlSerializer.ManualSerialization.CONSTRUCTOR)
    override val particleEffect: ParticleEffectData = ParticleEffectData()
            .setEffectType<ParticleEffectMeta<Location, Player, Material>>(ParticleEffectMeta.ParticleEffectType.EXPLOSION_NORMAL)
            .setAmount<ParticleEffectMeta<Location, Player, Material>>(4)
            .setSpeed<ParticleEffectMeta<Location, Player, Material>>(0.0002)
            .setOffset(2.0, 2.0, 2.0)
    /** SoundEffect being played when activating this.*/
    @YamlSerializer.YamlSerialize(orderNumber = 6, value = "sound-effect", classicSerialize = YamlSerializer.ManualSerialization.CONSTRUCTOR)
    override val soundEffect: SoundBuilder = SoundBuilder()
            .setName("GHAST_FIREBALL")
            .setVolume(100.0)
            .setPitch(1.0)
}