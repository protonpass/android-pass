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

package proton.android.pass.features.item.history.navigation

import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.items.ItemCategory

sealed interface ItemHistoryNavDestination {

    data object CloseScreen : ItemHistoryNavDestination

    @JvmInline
    value class Detail(val itemCategory: ItemCategory) : ItemHistoryNavDestination

    data class Restore(
        val shareId: ShareId,
        val itemId: ItemId,
        val itemRevision: ItemRevision
    ) : ItemHistoryNavDestination

    data class Timeline(
        val shareId: ShareId,
        val itemId: ItemId
    ) : ItemHistoryNavDestination

    @JvmInline
    value class PasskeyDetail(val passkey: UIPasskeyContent) : ItemHistoryNavDestination

    data class Options(
        val shareId: ShareId,
        val itemId: ItemId
    ) : ItemHistoryNavDestination

    data class ConfirmResetHistory(
        val shareId: ShareId,
        val itemId: ItemId
    ) : ItemHistoryNavDestination

    @JvmInline
    value class WifiNetworkQR(val rawSVG: String) : ItemHistoryNavDestination

    data object Upgrade : ItemHistoryNavDestination
}
