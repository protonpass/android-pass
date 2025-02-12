/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.data.impl.usecases.shares

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.shares.ObserveHasShares
import proton.android.pass.domain.ShareType
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

class ObserveHasSharesImpl @Inject constructor(
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    private val observeCurrentUser: ObserveCurrentUser,
    private val shareRepository: ShareRepository
) : ObserveHasShares {

    override fun invoke(): Flow<Boolean> = combine(
        featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.ITEM_SHARING_V1),
        observeCurrentUser()
    ) { isItemSharingEnabled, user ->
        if (isItemSharingEnabled) {
            shareRepository.observeAllShares(userId = user.userId)
        } else {
            shareRepository.observeSharesByType(
                userId = user.userId,
                shareType = ShareType.Vault,
                isActive = true
            )
        }
    }.flatMapLatest { sharesFlow ->
        sharesFlow.map { shares -> shares.isNotEmpty() }
    }

}
