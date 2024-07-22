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

package proton.android.pass.features.item.details.detailforbidden.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultUnspecified
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.dialogs.DialogCancelConfirmSection
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.features.item.details.detailforbidden.presentation.ItemDetailsForbiddenState
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun ItemDetailsForbiddenContent(
    modifier: Modifier = Modifier,
    onEvent: (ItemDetailsForbiddenUiEvent) -> Unit,
    state: ItemDetailsForbiddenState
) = with(state) {
    NoPaddingDialog(
        modifier = modifier,
        onDismissRequest = {
            ItemDetailsForbiddenUiEvent.OnDismiss
                .also(onEvent)
        }
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = 24.dp,
                    top = 24.dp,
                    end = 24.dp,
                    bottom = Spacing.mediumSmall
                ),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
        ) {
            ProtonDialogTitle(title = stringResource(title))

            Text(
                text = stringResource(id = message),
                style = ProtonTheme.typography.defaultUnspecified
            )

            DialogCancelConfirmSection(
                color = PassTheme.colors.loginInteractionNormMajor1,
                confirmText = if (showUpgrade) {
                    stringResource(CompR.string.action_upgrade_now)
                } else {
                    stringResource(CoreR.string.presentation_alert_ok)
                },
                cancelText = stringResource(CoreR.string.presentation_alert_cancel)
                    .takeIf { showUpgrade }
                    .orEmpty(),
                onDismiss = {
                    ItemDetailsForbiddenUiEvent.OnDismiss
                        .also(onEvent)
                },
                onConfirm = {
                    if (showUpgrade) {
                        ItemDetailsForbiddenUiEvent.OnUpgrade
                    } else {
                        ItemDetailsForbiddenUiEvent.OnCancel
                    }.also(onEvent)
                }
            )
        }
    }
}
