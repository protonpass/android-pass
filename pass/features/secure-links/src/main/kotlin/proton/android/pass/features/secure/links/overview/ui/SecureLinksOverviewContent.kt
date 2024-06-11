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

package proton.android.pass.features.secure.links.overview.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTopBarBackButtonType
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.secure.links.R
import proton.android.pass.features.secure.links.overview.presentation.SecureLinksOverviewState
import proton.android.pass.features.secure.links.overview.ui.widgets.SecureLinksOverviewInfoWidget
import proton.android.pass.features.secure.links.overview.ui.widgets.SecureLinksOverviewLinkWidget
import proton.android.pass.features.secure.links.shared.presentation.SecureLink
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SecureLinksOverviewContent(
    modifier: Modifier = Modifier,
    onUiEvent: (SecureLinksOverviewUiEvent) -> Unit,
    state: SecureLinksOverviewState
) = with(state) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PassExtendedTopBar(
                backButton = PassTopBarBackButtonType.Cross,
                onUpClick = { onUiEvent(SecureLinksOverviewUiEvent.OnCloseClicked) }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.padding(all = Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.small),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PassCircleButton(
                    text = stringResource(id = R.string.secure_links_overview_button_copy_link),
                    onClick = { onUiEvent(SecureLinksOverviewUiEvent.OnCopyLinkClicked) }
                )

                PassCircleButton(
                    text = stringResource(id = R.string.secure_links_overview_button_share_link),
                    textColor = PassTheme.colors.interactionNormMajor2,
                    backgroundColor = PassTheme.colors.interactionNormMinor1,
                    onClick = { onUiEvent(SecureLinksOverviewUiEvent.OnShareLinkClicked) }
                )

                TextButton(
                    onClick = { onUiEvent(SecureLinksOverviewUiEvent.OnViewAllLinksClicked) }
                ) {
                    Text(
                        text = stringResource(id = R.string.secure_links_overview_button_view_all_links),
                        style = ProtonTheme.typography.defaultNorm,
                        color = PassTheme.colors.interactionNormMajor2
                    )
                }
            }
        }
    ) { innerPaddingValue ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPaddingValue)
                .padding(all = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
            ) {
                SecureLinksOverviewInfoWidget(
                    modifier = Modifier.weight(weight = 1f),
                    iconResId = CoreR.drawable.ic_proton_clock,
                    titleResId = R.string.secure_links_overview_widget_expiration_title,
                    infoText = SecureLink.expirationOptionsMap[expiration]
                        ?.let { expirationResId -> stringResource(id = expirationResId) }
                        ?: "",
                )

                SecureLinksOverviewInfoWidget(
                    modifier = Modifier.weight(weight = 1f),
                    iconResId = CoreR.drawable.ic_proton_eye,
                    titleResId = R.string.secure_links_overview_widget_max_views_title,
                    infoText = maxViewsAllows
                        ?.let { maxViews ->
                            pluralStringResource(
                                id = R.plurals.secure_links_overview_widget_max_views_limited,
                                count = maxViews,
                                maxViews
                            )
                        }
                        ?: stringResource(id = R.string.secure_links_overview_widget_max_views_unlimited),
                )
            }

            SecureLinksOverviewLinkWidget(secureLink = secureLink)
        }
    }
}
