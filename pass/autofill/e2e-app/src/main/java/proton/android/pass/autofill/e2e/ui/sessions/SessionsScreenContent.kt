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

package proton.android.pass.autofill.e2e.ui.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SessionsScreenContent(
    modifier: Modifier = Modifier,
    state: SessionsScreenUiState,
    onRefresh: () -> Unit,
    onClearSessions: () -> Unit,
    onSessionClick: (AutofillSession) -> Unit,
    onShareSessionClick: (AutofillSession) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize().systemBarsPadding(),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(title = { Text(text = "E2E App") })
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.medium, vertical = Spacing.mediumSmall),
                    onClick = onClearSessions
                ) {
                    Text(text = "Clear All")
                }
            }
        }
    ) { padding ->
        val isRefreshing = state.isLoading()
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = onRefresh,
            refreshThreshold = 40.dp
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(state = pullRefreshState)
                .padding(padding)
        ) {
            when (state) {
                is SessionsScreenUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Error: ${state.message}")
                    }
                }

                SessionsScreenUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    }
                }

                is SessionsScreenUiState.Content -> {
                    SessionsScreenContent(
                        sessions = state.sessions,
                        onSessionClick = onSessionClick,
                        onShareSessionClick = onShareSessionClick
                    )
                    PullRefreshIndicator(
                        refreshing = isRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionsScreenContent(
    modifier: Modifier = Modifier,
    sessions: ImmutableList<AutofillSession>,
    onSessionClick: (AutofillSession) -> Unit,
    onShareSessionClick: (AutofillSession) -> Unit
) {
    if (sessions.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No debug sessions")
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(items = sessions, key = { it.filename }) { session ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSessionClick(session) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AnnotatedString(
                                text = session.packageName,
                                spanStyle = SpanStyle(fontWeight = FontWeight.Bold)
                            ),
                            fontSize = 14.sp
                        )
                        Text(
                            text = session.timestamp,
                            fontSize = 12.sp
                        )

                    }

                    CircleIconButton(
                        backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                        onClick = { onShareSessionClick(session) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = PassTheme.colors.loginInteractionNormMajor2
                        )
                    }
                }
            }
        }
    }
}
