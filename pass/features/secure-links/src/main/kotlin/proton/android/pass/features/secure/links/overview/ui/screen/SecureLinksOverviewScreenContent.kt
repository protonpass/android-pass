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

package proton.android.pass.features.secure.links.overview.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTopBarBackButtonType
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.secure.links.R
import proton.android.pass.features.secure.links.overview.presentation.SecureLinksOverviewState
import proton.android.pass.features.secure.links.overview.ui.shared.events.SecureLinksOverviewUiEvent
import proton.android.pass.features.secure.links.overview.ui.shared.footers.SecureLinksOverviewFooter
import proton.android.pass.features.secure.links.overview.ui.shared.headers.SecureLinksOverviewHeader
import proton.android.pass.features.secure.links.overview.ui.shared.widgets.SecureLinksOverviewWidget

@Composable
internal fun SecureLinksOverviewScreenContent(
    modifier: Modifier = Modifier,
    onUiEvent: (SecureLinksOverviewUiEvent) -> Unit,
    state: SecureLinksOverviewState
) = with(state) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            PassExtendedTopBar(
                backButton = PassTopBarBackButtonType.Cross,
                onUpClick = {
                    itemUiModel?.category?.let { itemCategory ->
                        SecureLinksOverviewUiEvent.OnCloseClicked(
                            itemCategory = itemCategory
                        ).also(onUiEvent)
                    }
                }
            )
        },
        bottomBar = {
            SecureLinksOverviewFooter(
                modifier = Modifier.padding(all = Spacing.medium),
                linkTextResId = R.string.secure_links_overview_button_view_all_links,
                onCopyLinkClicked = { onUiEvent(SecureLinksOverviewUiEvent.OnCopyLinkClicked) },
                onShareLinkClicked = { onUiEvent(SecureLinksOverviewUiEvent.OnShareLinkClicked) },
                onLinkClicked = { onUiEvent(SecureLinksOverviewUiEvent.OnViewAllLinksClicked) }
            )
        }
    ) { innerPaddingValue ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPaddingValue)
                .padding(horizontal = Spacing.medium),
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
                viewsTitleResId = R.string.secure_links_overview_widget_max_views_title,
                remainingTime = remainingTime,
                linkUrl = secureLinkUrl,
                viewsText = maxViewsAllowed
                    ?.let { maxViews ->
                        pluralStringResource(
                            id = R.plurals.secure_links_overview_widget_max_views_limited,
                            count = maxViews,
                            maxViews
                        )
                    }
                    ?: stringResource(id = R.string.secure_links_overview_widget_max_views_unlimited)
            )
        }
    }
}
