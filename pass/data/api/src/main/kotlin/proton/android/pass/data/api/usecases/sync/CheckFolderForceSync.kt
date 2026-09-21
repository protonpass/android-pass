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

package proton.android.pass.data.api.usecases.sync

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.SyncMode

/**
 * Runs the folders repair force sync for [userId] if it is still owed. Safe to call on every sync
 * trigger. Callers the user did not open Pass for must pass [SyncMode.Background] so the repair
 * does not surface the sync dialog.
 */
interface CheckFolderForceSync {
    suspend operator fun invoke(userId: UserId, syncMode: SyncMode = SyncMode.ShownToUser)
}
