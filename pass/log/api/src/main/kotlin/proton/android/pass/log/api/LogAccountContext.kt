/*
 * Copyright (c) 2026 Proton AG
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

import kotlinx.coroutines.ThreadContextElement
import me.proton.core.domain.entity.UserId
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Tags every [PassLogger] call made within a coroutine as belonging to [userId], so the file
 * logger can route the log line to that account's own log file instead of whichever account
 * happens to be primary at write time. Needed because background jobs (account setup, sync,
 * cleanup) can run for a non-primary account while another account is primary.
 */
class LogAccountContext(private val userId: UserId) :
    ThreadContextElement<UserId?>,
    AbstractCoroutineContextElement(Key) {

    override fun updateThreadContext(context: CoroutineContext): UserId? {
        val previous = threadLocalUserId.get()
        threadLocalUserId.set(userId)
        return previous
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: UserId?) {
        threadLocalUserId.set(oldState)
    }

    companion object Key : CoroutineContext.Key<LogAccountContext> {
        private val threadLocalUserId = ThreadLocal<UserId?>()

        fun current(): UserId? = threadLocalUserId.get()
    }
}
