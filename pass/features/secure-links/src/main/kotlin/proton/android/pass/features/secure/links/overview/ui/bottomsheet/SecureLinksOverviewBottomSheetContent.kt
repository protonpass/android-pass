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

package proton.android.pass.features.secure.links.overview.ui.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.secure.links.R
import proton.android.pass.features.secure.links.overview.presentation.SecureLinksOverviewState
import proton.android.pass.features.secure.links.overview.ui.shared.SecureLinksOverviewUiEvent
import proton.android.pass.features.secure.links.overview.ui.shared.footers.SecureLinksOverviewFooter
import proton.android.pass.features.secure.links.overview.ui.shared.headers.SecureLinksOverviewHeader
import proton.android.pass.features.secure.links.overview.ui.shared.widgets.SecureLinksOverviewWidget

@Composable
internal fun SecureLinksOverviewBottomSheetContent(
    modifier: Modifier = Modifier,
    onUiEvent: (SecureLinksOverviewUiEvent) -> Unit,
    state: SecureLinksOverviewState
) = with(state) {
    Column(
        modifier = modifier.padding(all = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        itemUiModel?.let { item ->
            SecureLinksOverviewHeader(
                item = item,
                shareIcon = shareIcon,
                canLoadExternalImages = canLoadExternalImages
            )
        }

        SecureLinksOverviewWidget(
            remainingTime = remainingTime,
            currentViews = currentViews,
            maxViewsAllowed = maxViewsAllowed,
            linkUrl = secureLinkUrl
        )

        SecureLinksOverviewFooter(
            linkTextResId = R.string.secure_links_overview_button_delete_link,
            linkTextColor = PassTheme.colors.passwordInteractionNormMajor2,
            onCopyLinkClicked = { onUiEvent(SecureLinksOverviewUiEvent.OnCopyLinkClicked) },
            onShareLinkClicked = { onUiEvent(SecureLinksOverviewUiEvent.OnShareLinkClicked) },
            onLinkClicked = { onUiEvent(SecureLinksOverviewUiEvent.OnDeleteLinkClicked) },
        )
    }
}
