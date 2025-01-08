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

package proton.android.pass.features.secure.links.overview.presentation

import com.google.android.material.bottomsheet.BottomSheetBehavior.State
import kotlinx.datetime.Instant
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.Vault
import proton.android.pass.domain.time.RemainingTime

@State
internal data class SecureLinksOverviewState(
    internal val secureLinkUrl: String,
    internal val currentViews: Int,
    internal val maxViewsAllowed: Int?,
    internal val itemUiModel: ItemUiModel?,
    internal val canLoadExternalImages: Boolean,
    internal val vaultOption: Option<Vault>,
    internal val event: SecureLinksOverviewEvent,
    private val expirationSeconds: Long,
    private val isDeletingLoadingState: IsLoadingState
) {

    internal val shareIcon: ShareIcon? = vaultOption.value()?.icon

    internal val remainingTime: RemainingTime = RemainingTime(
        endInstant = Instant.fromEpochSeconds(expirationSeconds)
    )

    internal val isDeleting: Boolean = isDeletingLoadingState is IsLoadingState.Loading

    internal companion object {

        internal val Initial: SecureLinksOverviewState = SecureLinksOverviewState(
            secureLinkUrl = "",
            currentViews = 0,
            maxViewsAllowed = null,
            itemUiModel = null,
            canLoadExternalImages = false,
            vaultOption = None,
            event = SecureLinksOverviewEvent.Idle,
            expirationSeconds = 0L,
            isDeletingLoadingState = IsLoadingState.NotLoading
        )

    }

}
