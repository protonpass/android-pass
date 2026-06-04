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

package proton.android.pass.data.api.usecases

enum class ReconcileAppLockResult {
    /** Lock state is consistent / restored; proceed with normal auth gate. */
    Ok,

    /** Proto holds a lock the durable store is missing; caller must run [SeedAppLockStore] off the auth gate. */
    SeedRequired,

    /** Lock state is corrupt and indeterminable; caller must force re-authentication. */
    RequireReauth
}

interface ReconcileAppLock {
    suspend operator fun invoke(): ReconcileAppLockResult
}
