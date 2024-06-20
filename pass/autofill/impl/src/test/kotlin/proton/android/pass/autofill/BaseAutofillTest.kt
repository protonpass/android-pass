/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.autofill

import android.util.Log
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.Logger
import org.junit.Before

open class BaseAutofillTest(val priority: Int = Log.DEBUG) {

    @Before
    fun setup() {
        val logger = object : Logger {
            override fun d(tag: String, message: String) {
                if (priority < Log.DEBUG) return
                println("D: $tag: $message")
            }

            override fun d(
                tag: String,
                e: Throwable,
                message: String
            ) {
                if (priority < Log.DEBUG) return
                println("D: $tag: $message : $e")
            }

            override fun e(tag: String, message: String) {
                if (priority < Log.ERROR) return
                println("E: $tag: $message")
            }

            override fun e(tag: String, e: Throwable) {
                if (priority < Log.ERROR) return
                println("E: $tag: $e")
            }

            override fun e(
                tag: String,
                e: Throwable,
                message: String
            ) {
                if (priority < Log.ERROR) return
                println("E: $tag: $message : $e")
            }

            override fun i(tag: String, message: String) {
                if (priority < Log.INFO) return
                println("I: $tag: $message")
            }

            override fun i(
                tag: String,
                e: Throwable,
                message: String
            ) {
                if (priority < Log.INFO) return
                println("I: $tag: $message : $e")
            }

            override fun v(tag: String, message: String) {
                if (priority < Log.VERBOSE) return
                println("V: $tag: $message")
            }

            override fun v(
                tag: String,
                e: Throwable,
                message: String
            ) {
                if (priority < Log.VERBOSE) return
                println("V: $tag: $message : $e")
            }

            override fun w(tag: String, message: String) {
                if (priority < Log.WARN) return
                println("W: $tag: $message")
            }

            override fun w(tag: String, e: Throwable) {
                if (priority < Log.WARN) return
                println("W: $tag: $e")
            }

            override fun w(
                tag: String,
                e: Throwable,
                message: String
            ) {
                if (priority < Log.WARN) return
                println("W: $tag: $message : $e")
            }
        }

        CoreLogger.set(logger)
    }
}
