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

package proton.android.pass.data.fakes.usecases.folders

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.folders.FolderPresence
import proton.android.pass.data.api.usecases.folders.HasAnyFolders
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeHasAnyFolders @Inject constructor() : HasAnyFolders {

    data class Invocation(
        val userId: UserId,
        val shareIds: Set<ShareId>
    )

    val invocations = mutableListOf<Invocation>()
    private var result: FolderPresence = FolderPresence.NoFolders
    private var throwable: Throwable? = null
    private var onInvoke: (suspend () -> Unit)? = null

    fun setResult(value: FolderPresence) {
        result = value
    }

    fun setThrowable(value: Throwable?) {
        throwable = value
    }

    fun setOnInvoke(value: suspend () -> Unit) {
        onInvoke = value
    }

    override suspend fun invoke(userId: UserId, shareIds: Set<ShareId>): FolderPresence {
        invocations += Invocation(userId, shareIds)
        onInvoke?.invoke()
        throwable?.let { error -> throw error }
        return result
    }
}
