package com.github.shynixn.blockball.bukkit.logic.persistence.entity.meta.effect;

import com.github.shynixn.blockball.api.persistence.entity.meta.effect.GlowEffectMeta;
import com.github.shynixn.blockball.bukkit.logic.persistence.entity.PersistenceObject;
import com.github.shynixn.blockball.bukkit.logic.business.helper.YamlSerializer;

/**
 * Created by Shynixn 2017.
 * <p>
 * Version 1.1
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017 by Shynixn
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
public class GlowEffectProperties extends PersistenceObject implements GlowEffectMeta {

    @YamlSerializer.YamlSerialize(orderNumber = 1, value = "enabled")
    private boolean enabled;

    @YamlSerializer.YamlSerialize(orderNumber = 2, value = "duration")
    private int duration;

    /**
     * Sets the amount of seconds a player is glowing.
     *
     * @param amount amount
     */
    @Override
    public void setAmountOfSeconds(int amount) {
        this.duration = amount;
    }

    /**
     * Returns the amount of seconds a player is glowing.
     *
     * @return amount
     */
    @Override
    public int getAmountOfSeconds() {
        return this.duration;
    }

    /**
     * Toggles the effect.
     *
     * @param enabled enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns if the effect is enabled
     *
     * @return enabled
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
