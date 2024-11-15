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

package proton.android.pass.features.sharing.sharingsummary

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.util.kotlin.takeIfNotBlank
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.masks.TextMask
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.repositories.AddressPermission
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.sharing.common.AddressPermissionUiState
import proton.android.pass.features.sharing.common.toUiState
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.preferences.value

internal sealed interface SharingSummaryEvent {

    data object Idle : SharingSummaryEvent

    data object OnGoHome : SharingSummaryEvent

    @JvmInline
    value class OnSharingItemSuccess(internal val itemCategory: ItemCategory) : SharingSummaryEvent

    data object OnSharingVaultError : SharingSummaryEvent

    @JvmInline
    value class OnSharingVaultSuccess(internal val shareId: ShareId) : SharingSummaryEvent

}

@Stable
internal sealed class SharingSummaryState {

    internal abstract val event: SharingSummaryEvent

    internal abstract val addressPermissions: List<AddressPermission>

    protected abstract val isLoadingState: IsLoadingState

    internal val addresses: ImmutableList<AddressPermissionUiState>
        get() = addressPermissions
            .map(AddressPermission::toUiState)
            .toPersistentList()

    internal val isLoading: Boolean
        get() = isLoadingState.value()


    data object Initial : SharingSummaryState() {

        override val addressPermissions: List<AddressPermission> = emptyList()

        override val isLoadingState: IsLoadingState = IsLoadingState.NotLoading

        override val event: SharingSummaryEvent = SharingSummaryEvent.Idle

    }

    data class ShareItem(
        override val event: SharingSummaryEvent,
        override val addressPermissions: List<AddressPermission>,
        override val isLoadingState: IsLoadingState,
        private val itemUiModel: ItemUiModel,
        private val useFaviconsPreference: UseFaviconsPreference
    ) : SharingSummaryState() {

        internal val itemCategory: ItemCategory = itemUiModel.category

        internal val itemTitle: String = itemUiModel.contents.title

        internal val itemSubtitle: String? = when (itemCategory) {
            ItemCategory.CreditCard -> TextMask.CardNumber(itemUiModel.contents.displayValue).masked
            ItemCategory.Alias,
            ItemCategory.Login,
            ItemCategory.Note,
            ItemCategory.Identity,
            ItemCategory.Password,
            ItemCategory.Unknown -> itemUiModel.contents.displayValue
        }.takeIfNotBlank()

        internal val itemPackageName: String = when (val contents = itemUiModel.contents) {
            is ItemContents.Login -> contents.packageName.orEmpty()
            is ItemContents.Alias,
            is ItemContents.CreditCard,
            is ItemContents.Identity,
            is ItemContents.Note,
            is ItemContents.Unknown -> ""
        }

        internal val itemWebsite: String = when (val contents = itemUiModel.contents) {
            is ItemContents.Login -> contents.websiteUrl.orEmpty()
            is ItemContents.Alias,
            is ItemContents.CreditCard,
            is ItemContents.Identity,
            is ItemContents.Note,
            is ItemContents.Unknown -> ""
        }

        internal val canItemLoadExternalImages: Boolean = useFaviconsPreference.value()

    }

    data class ShareVault(
        override val event: SharingSummaryEvent,
        override val addressPermissions: List<AddressPermission>,
        override val isLoadingState: IsLoadingState,
        private val vaultWithItemCount: VaultWithItemCount
    ) : SharingSummaryState() {

        internal val vault: Vault = vaultWithItemCount.vault

        internal val vaultItemCount: Int = vaultWithItemCount.activeItemCount
            .plus(vaultWithItemCount.trashedItemCount)
            .toInt()

    }

}
