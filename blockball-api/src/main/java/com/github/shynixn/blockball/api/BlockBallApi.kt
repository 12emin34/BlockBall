@file:Suppress("unused")

package com.github.shynixn.blockball.api

import com.github.shynixn.blockball.api.business.controller.BungeeCordConnectionController
import com.github.shynixn.blockball.api.business.controller.GameController
import com.github.shynixn.blockball.api.business.entity.BlockBallPlugin

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
object BlockBallApi {
    private var bungeeCordConnectController: BungeeCordConnectionController<*>? = null
    private var gameController: GameController<*, *, *, *>? = null
    private var plugin: BlockBallPlugin? = null

    private fun initializeBlockBall(gameController: Any, bungeeCordConnectionController: Any, blockBallPlugin: BlockBallPlugin) {
        this.gameController = gameController as GameController<*, *, *, *>
        this.bungeeCordConnectController = bungeeCordConnectionController as BungeeCordConnectionController<*>
        this.plugin = blockBallPlugin
    }

    /**
     * Gets a business logic service by resolving the given class.
     */
    fun <S> resolve(service: Class<S>): S {
        return plugin!!.resolve(service)
    }

    /**
     * Returns the default game controller.
     */
    fun <T : GameController<*, *, *, *>> getDefaultGameController(): T {
        return gameController as T
    }

    /**
     * Returns the default bungeecord controller.
     */
    fun <T : BungeeCordConnectionController<*>> getDefaultBungeeCordController(): T {
        return bungeeCordConnectController as T
    }
}