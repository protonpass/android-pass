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

package proton.android.pass.commonrust.impl

import proton.android.pass.commonrust.Share
import proton.android.pass.commonrust.ShareOverrideCalculator
import proton.android.pass.commonrust.TargetType
import proton.android.pass.commonrust.api.UsableShareFilter
import proton.android.pass.commonrust.api.UsableShareKey
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsableShareFilterImpl @Inject constructor(
    private val shareOverrideCalculator: ShareOverrideCalculator
) : UsableShareFilter {

    override fun filter(list: List<UsableShareKey>): List<ShareId> = shareOverrideCalculator.getVisibleShares(
        list.map {
            Share(
                shareId = it.shareId,
                vaultId = it.vaultId,
                targetType = when (it.targetType) {
                    ShareType.Vault -> TargetType.VAULT
                    ShareType.Item -> TargetType.ITEM
                },
                targetId = it.targetId,
                roleId = it.roleId,
                permissions = it.permissions.toUShort()
            )
        }
    ).map(::ShareId)
}
