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

package proton.android.pass.log.api

import me.proton.core.util.android.sentry.TimberLogger
import me.proton.core.util.kotlin.Logger
import me.proton.core.util.kotlin.LoggerLogTag
import timber.log.Timber

object PassLogger : Logger by TimberLogger {
    fun w(tag: String, e: Throwable) = Timber.tag(tag).w(e)
    fun w(tag: String, e: Throwable, message: String) = Timber.tag(tag).w(e, message)
    fun w(tag: String, message: String) = Timber.tag(tag).w(message)
    override fun log(tag: LoggerLogTag, message: String) = i(tag.name, message)
}
