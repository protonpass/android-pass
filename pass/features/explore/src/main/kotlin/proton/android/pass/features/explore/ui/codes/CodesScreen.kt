/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.explore.ui.codes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.masks.TextMask
import proton.android.pass.composecomponents.impl.item.highlight
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.progress.PassTotpProgress
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.topbar.CollapsibleSearchTopBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.features.explore.R
import proton.android.pass.features.explore.presentation.codes.CodeRow
import proton.android.pass.features.explore.presentation.codes.CodesUiEvent
import proton.android.pass.features.explore.presentation.codes.CodesUiState
import proton.android.pass.features.explore.presentation.codes.CodesViewModel

@Composable
fun CodesScreen(onUpClick: () -> Unit, viewModel: CodesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CodesContent(
        state = state,
        onUpClick = onUpClick,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CodesContent(
    state: CodesUiState,
    onUpClick: () -> Unit,
    onEvent: (CodesUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            CollapsibleSearchTopBar(
                scrollBehavior = scrollBehavior,
                title = stringResource(R.string.codes_screen_title),
                searchQuery = state.searchQuery,
                placeholderText = stringResource(R.string.codes_search_placeholder),
                inSearchMode = state.inSearchMode,
                onSearchQueryChange = { onEvent(CodesUiEvent.SearchQueryChange(it)) },
                onEnterSearch = { onEvent(CodesUiEvent.EnterSearch) },
                onStopSearch = { onEvent(CodesUiEvent.StopSearch) },
                drawerIcon = {
                    BackArrowCircleIconButton(
                        color = PassTheme.colors.interactionNormMajor2,
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onUpClick = {
                            if (state.inSearchMode) onEvent(CodesUiEvent.StopSearch) else onUpClick()
                        }
                    )
                },
                actions = {
                    if (state.showPerRowProgress) {
                        Spacer(
                            modifier = Modifier
                                .padding(end = Spacing.medium)
                                .size(40.dp)
                        )
                    } else {
                        PassTotpProgress(
                            modifier = Modifier
                                .padding(end = Spacing.medium)
                                .size(40.dp),
                            remainingSeconds = state.remainingSeconds,
                            totalSeconds = state.totalSeconds
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .background(PassTheme.colors.backgroundNorm)
                .padding(padding)
                .padding(horizontal = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            items(items = state.rows, key = CodeRow::rowId) { row ->
                CodeRowItem(
                    row = row,
                    searchQuery = state.searchQuery,
                    showProgress = state.showPerRowProgress,
                    onClick = { onEvent(CodesUiEvent.CodeClicked(row.code)) }
                )
            }
        }
    }
}

@Composable
private fun CodeRowItem(
    row: CodeRow,
    searchQuery: String,
    showProgress: Boolean,
    onClick: () -> Unit
) {
    val highlightColor = PassTheme.colors.interactionNorm
    val annotatedTitle = if (searchQuery.isBlank()) {
        AnnotatedString(row.title)
    } else {
        row.title.highlight(searchQuery, highlightColor) ?: AnnotatedString(row.title)
    }
    val annotatedSubtitle = if (searchQuery.isBlank()) {
        AnnotatedString(row.subtitle)
    } else {
        row.subtitle.highlight(searchQuery, highlightColor) ?: AnnotatedString(row.subtitle)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LoginIcon(
            text = row.title,
            websites = row.websites,
            packageName = row.packageName,
            canLoadExternalImages = false
        )
        Spacer(modifier = Modifier.width(Spacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Text.Body1Regular(
                annotatedText = annotatedTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text.Body3Weak(
                annotatedText = annotatedSubtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(Spacing.small))
        Text.Body1Regular(
            text = if (row.code.isEmpty()) "" else TextMask.TotpCode(row.code).masked,
            color = PassTheme.colors.textNorm
        )
        if (showProgress) {
            Spacer(modifier = Modifier.width(Spacing.small))
            PassTotpProgress(
                modifier = Modifier.size(40.dp),
                remainingSeconds = row.remainingSeconds,
                totalSeconds = row.totalSeconds
            )
        }
    }
}

internal class ThemeCodesContentPreviewProvider :
    ThemePairPreviewProvider<CodesUiState>(CodesContentPreviewProvider())

@Preview
@Composable
internal fun CodesContentPreview(
    @PreviewParameter(ThemeCodesContentPreviewProvider::class) input: Pair<Boolean, CodesUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CodesContent(
                state = input.second,
                onUpClick = {},
                onEvent = {}
            )
        }
    }
}


