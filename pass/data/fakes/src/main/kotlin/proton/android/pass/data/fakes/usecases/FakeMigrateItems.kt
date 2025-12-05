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

package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.repositories.MigrateItemsResult
import proton.android.pass.data.api.usecases.MigrateItems
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeMigrateItems @Inject constructor() : MigrateItems {

    private var result: Result<MigrateItemsResult> = Result.failure(IllegalStateException("Result not set"))

    private val memory = mutableListOf<Payload>()

    fun memory(): List<Payload> = memory

    fun setResult(value: Result<MigrateItemsResult>) {
        result = value
    }

    override suspend fun invoke(items: Map<ShareId, List<ItemId>>, destinationShare: ShareId): MigrateItemsResult {
        memory.add(Payload(items, destinationShare))
        return result.getOrThrow()
    }

    data class Payload(
        val items: Map<ShareId, List<ItemId>>,
        val destinationShare: ShareId
    )
}
