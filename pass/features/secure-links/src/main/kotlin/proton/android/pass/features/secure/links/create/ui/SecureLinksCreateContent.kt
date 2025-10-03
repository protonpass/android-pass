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

package proton.android.pass.features.secure.links.create.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentMapOf
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.domain.securelinks.SecureLinkExpiration
import proton.android.pass.features.secure.links.R
import proton.android.pass.features.secure.links.create.presentation.SecureLinksCreateState
import proton.android.pass.features.secure.links.create.ui.dialogs.SecureLinkCreateExpirationDialog
import proton.android.pass.features.secure.links.create.ui.rows.SecureLinkCreateExpirationRow
import proton.android.pass.features.secure.links.create.ui.rows.SecureLinkCreateMaxViewsRow

@Composable
internal fun SecureLinksCreateContent(
    modifier: Modifier = Modifier,
    state: SecureLinksCreateState,
    shouldDisplayExpirationDialog: Boolean,
    onUiEvent: (SecureLinksCreateUiEvent) -> Unit
) = with(state) {
    val expirationOptionsMap = remember {
        persistentMapOf(
            SecureLinkExpiration.OneHour to R.string.secure_links_create_row_expiration_options_one_hour,
            SecureLinkExpiration.OneDay to R.string.secure_links_create_row_expiration_options_one_day,
            SecureLinkExpiration.SevenDays to R.string.secure_links_create_row_expiration_options_seven_days,
            SecureLinkExpiration.FourteenDays to R.string.secure_links_create_row_expiration_options_fourteen_days,
            SecureLinkExpiration.ThirtyDays to R.string.secure_links_create_row_expiration_options_thirty_days
        )
    }

    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            PassExtendedTopBar(
                title = stringResource(id = R.string.secure_links_create_title),
                onUpClick = { onUiEvent(SecureLinksCreateUiEvent.OnBackArrowClicked) }
            )
        },
        bottomBar = {
            PassCircleButton(
                modifier = Modifier.padding(all = Spacing.medium),
                text = stringResource(id = R.string.secure_links_create_button_generate),
                isLoading = isLoading,
                onClick = { onUiEvent(SecureLinksCreateUiEvent.OnGenerateLinkClicked) }
            )
        }
    ) { innerPaddingValue ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPaddingValue)
                .padding(top = Spacing.medium)
        ) {
            SecureLinkCreateExpirationRow(
                isConfigurationAllowed = isConfigurationAllowed,
                expirationText = expirationOptionsMap[expiration]
                    ?.let { expirationResId -> stringResource(id = expirationResId) }
                    ?: "",
                onUiEvent = onUiEvent
            )

            SecureLinkCreateMaxViewsRow(
                isConfigurationAllowed = isConfigurationAllowed,
                isMaxViewsEnabled = isMaxViewsEnabled,
                isMaxViewsDecreaseEnabled = isMaxViewsDecreaseEnabled,
                maxViewsAllowed = maxViewsAllowed,
                onUiEvent = onUiEvent
            )
        }
    }

    if (shouldDisplayExpirationDialog) {
        SecureLinkCreateExpirationDialog(
            selectedExpiration = expiration,
            expirationOptionsMap = expirationOptionsMap,
            onUiEvent = onUiEvent
        )
    }
}
