/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.overlineNorm
import me.proton.core.presentation.R as CoreR
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.dialogs.ConfirmDialog
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LogViewContent(
    modifier: Modifier = Modifier,
    content: LogViewUiState,
    onUpClick: () -> Unit,
    onShareLogsClick: () -> Unit,
    onClearLogsClick: () -> Unit,
    onRefreshLogs: () -> Unit,
    onLoadOlderLogsClick: () -> Unit,
    onClearLogsDismiss: () -> Unit,
    onClearLogsConfirm: () -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = content.isRefreshing,
        onRefresh = onRefreshLogs
    )

    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(R.string.view_logs_title),
                onUpClick = onUpClick,
                actions = {
                    Circle(
                        modifier = Modifier
                            .padding(vertical = Spacing.extraSmall)
                            .padding(end = Spacing.small),
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onClick = onClearLogsClick
                    ) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_proton_trash),
                            contentDescription = stringResource(R.string.view_logs_clear_logs),
                            tint = PassTheme.colors.interactionNormMajor2
                        )
                    }
                    Circle(
                        modifier = Modifier.padding(vertical = Spacing.extraSmall),
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onClick = onShareLogsClick
                    ) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_proton_arrow_up_from_square),
                            contentDescription = stringResource(R.string.view_logs_share_logs),
                            tint = PassTheme.colors.interactionNormMajor2
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PassTheme.colors.backgroundStrong)
                .pullRefresh(pullRefreshState)
        ) {
            if (content.lines.isEmpty()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.view_logs_empty),
                    style = ProtonTheme.typography.overlineNorm
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.medium,
                        top = contentPadding.calculateTopPadding() + Spacing.medium,
                        end = Spacing.medium,
                        bottom = contentPadding.calculateBottomPadding() + Spacing.medium
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                ) {
                    items(
                        items = content.lines,
                        key = { line -> line.id }
                    ) { line ->
                        SelectionContainer {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = line.text,
                                style = ProtonTheme.typography.overlineNorm
                            )
                        }
                    }

                    if (content.hasOlderLogs || content.isLoadingOlder) {
                        item(key = "load-older") {
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !content.isLoadingOlder,
                                onClick = onLoadOlderLogsClick
                            ) {
                                Text(
                                    text = stringResource(
                                        if (content.isLoadingOlder) {
                                            R.string.view_logs_loading_older_logs
                                        } else {
                                            R.string.view_logs_load_older_logs
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = content.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    ConfirmDialog(
        title = stringResource(R.string.view_logs_clear_logs_confirmation_title),
        message = stringResource(R.string.view_logs_clear_logs_confirmation_message),
        confirmText = stringResource(R.string.view_logs_clear_logs),
        state = content.showClearLogsDialog.takeIf { it },
        onDismiss = onClearLogsDismiss,
        onConfirm = { onClearLogsConfirm() }
    )
}
