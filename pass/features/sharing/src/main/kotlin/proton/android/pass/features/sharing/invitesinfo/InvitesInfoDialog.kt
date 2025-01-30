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

package proton.android.pass.features.sharing.invitesinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultUnspecified
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.dialogs.DialogCancelConfirmSection
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.SharingNavigation

@Composable
fun InvitesInfoDialog(
    modifier: Modifier = Modifier,
    onNavigateEvent: (SharingNavigation) -> Unit,
    viewModel: InvitesInfoViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    NoPaddingDialog(
        modifier = modifier,
        onDismissRequest = {}
    ) {
        Column {
            when (val content = state) {
                is LoadingResult.Error -> {
                    onNavigateEvent(SharingNavigation.CloseDialog(false))
                }

                LoadingResult.Loading -> {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(top = 24.dp, bottom = Spacing.mediumSmall),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is LoadingResult.Success -> {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(top = 24.dp, bottom = Spacing.mediumSmall),
                        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        ProtonDialogTitle(
                            title = stringResource(R.string.sharing_invites_info_dialog_title)
                        )
                        Text(
                            text = pluralStringResource(
                                id = R.plurals.sharing_invites_info_dialog_message,
                                count = content.data.remainingInvites,
                                content.data.maxMembers,
                                content.data.remainingInvites
                            ),
                            style = ProtonTheme.typography.defaultUnspecified
                        )
                    }

                    DialogCancelConfirmSection(
                        modifier = Modifier.padding(Spacing.medium),
                        color = PassTheme.colors.loginInteractionNormMajor1,
                        cancelText = "",
                        onDismiss = {},
                        onConfirm = { onNavigateEvent(SharingNavigation.CloseDialog(false)) }
                    )
                }
            }
        }
    }
}
