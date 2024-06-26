/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.usecases.vaults

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.vaults.ObserveVaultsGroupedByShareId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import javax.inject.Inject

class ObserveVaultsGroupedByShareIdImpl @Inject constructor(
    private val observeVaults: ObserveVaults
) : ObserveVaultsGroupedByShareId {

    override fun invoke(): Flow<Map<ShareId, Vault>> = observeVaults()
        .map { vaults -> vaults.associateBy { vault -> vault.shareId } }

}
