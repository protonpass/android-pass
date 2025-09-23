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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.extensions.toVault
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.shares.ObserveSharesByType
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.Vault
import proton.android.pass.domain.sorted
import javax.inject.Inject

class ObserveVaultsImpl @Inject constructor(
    private val observeSharesByType: ObserveSharesByType
) : ObserveVaults {

    override fun invoke(userId: UserId?, includeHidden: Boolean): Flow<List<Vault>> =
        observeSharesByType(userId = userId, shareType = ShareType.Vault, includeHidden = includeHidden)
            .mapLatest { vaultShares ->
                vaultShares
                    .mapNotNull { vaultShare ->
                        vaultShare.toVault().value()
                    }
                    .sorted()

            }

}
