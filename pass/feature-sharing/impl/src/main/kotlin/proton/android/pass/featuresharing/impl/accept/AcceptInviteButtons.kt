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

package proton.android.pass.featuresharing.impl.accept

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.featuresharing.impl.R

@Composable
fun AcceptInviteButtons(
    modifier: Modifier = Modifier,
    isConfirmLoading: Boolean,
    isRejectLoading: Boolean,
    areButtonsEnabled: Boolean,
    showReject: Boolean,
    onConfirm: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LoadingCircleButton(
            modifier = Modifier.fillMaxWidth(),
            color = PassTheme.colors.interactionNormMajor1,
            isLoading = isConfirmLoading,
            buttonEnabled = areButtonsEnabled,
            text = {
                Text(
                    text = stringResource(R.string.sharing_join_shared_vault),
                    style = ProtonTheme.typography.defaultSmallNorm,
                    color = PassTheme.colors.interactionNormContrast
                )
            },
            onClick = onConfirm
        )

        AnimatedVisibility(visible = showReject) {
            LoadingCircleButton(
                modifier = Modifier.fillMaxWidth(),
                color = PassTheme.colors.interactionNormMinor1,
                isLoading = isRejectLoading,
                buttonEnabled = areButtonsEnabled,
                text = {
                    Text(
                        text = stringResource(R.string.sharing_reject_invitation),
                        style = ProtonTheme.typography.defaultSmallNorm,
                        color = PassTheme.colors.interactionNormMajor1
                    )
                },
                onClick = onReject
            )
        }
    }
}

@Preview
@Composable
fun AcceptInviteButtonsPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (confirm, reject) = input.second to !input.second
    PassTheme(isDark = input.first) {
        Surface {
            AcceptInviteButtons(
                isConfirmLoading = confirm,
                isRejectLoading = reject,
                areButtonsEnabled = true,
                showReject = true,
                onConfirm = {},
                onReject = {}
            )
        }
    }
}
