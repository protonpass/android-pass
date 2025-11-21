/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.password.history.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.item.icon.ThreeDotsMenuButton
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.domain.PasswordHistoryEntryId
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.password.R
import proton.android.pass.features.password.history.model.PasswordHistoryItemUiState
import proton.android.pass.features.password.history.model.PasswordHistoryUiState

@Composable
internal fun PasswordHistoryEntryContent(
    state: PasswordHistoryUiState,
    onBackClick: () -> Unit,
    onCopyPassword: (PasswordHistoryEntryId) -> Unit,
    onHideItem: (PasswordHistoryEntryId) -> Unit,
    onRevealItem: (PasswordHistoryEntryId) -> Unit,
    onMainThreeDotsMenuButtonClick: () -> Unit,
    onThreeDotsMenuButtonClick: (PasswordHistoryEntryId) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
        topBar = {
            BackArrowTopAppBar(
                modifier = Modifier,
                onUpClick = onBackClick,
                arrowColor = PassTheme.colors.passwordInteractionNormMajor2,
                backgroundArrowColor = PassTheme.colors.passwordInteractionNormMinor1,
                actions = {
                    ThreeDotsMenuButton(
                        modifier = Modifier.size(size = 40.dp),
                        onClick = onMainThreeDotsMenuButtonClick,
                        backgroundColor = PassTheme.colors.passwordInteractionNormMinor1,
                        dotsColor = PassTheme.colors.passwordInteractionNormMajor2
                    )
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(Spacing.medium))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium)
            ) {
                Text.Hero(
                    text = stringResource(R.string.password_history_title)
                )

                AnimatedVisibility(
                    visible = !state.isLoading && state.items.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text.Headline(
                                text = stringResource(R.string.password_history_empty),
                                color = PassTheme.colors.textWeak,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(Spacing.medium))

                            Text.Body1Regular(
                                text = stringResource(R.string.password_history_description),
                                color = PassTheme.colors.textWeak,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !state.isLoading && state.items.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(top = Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(space = Spacing.mediumSmall),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(
                            items = state.items,
                            key = { it.passwordHistoryEntryId.id }
                        ) { item ->
                            PasswordHistoryItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                item = item,
                                onChangeVisibility = { mustReveal ->
                                    if (mustReveal) {
                                        onRevealItem(item.passwordHistoryEntryId)
                                    } else {
                                        onHideItem(item.passwordHistoryEntryId)
                                    }
                                },
                                onThreeDotsClick = {
                                    onThreeDotsMenuButtonClick(item.passwordHistoryEntryId)
                                },
                                onPasswordClick = {
                                    onCopyPassword(item.passwordHistoryEntryId)
                                }
                            )
                        }


                        item {
                            Text.Body1Regular(
                                modifier = Modifier.animateItem(),
                                text = stringResource(R.string.password_history_description),
                                color = PassTheme.colors.textWeak,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }


                AnimatedVisibility(
                    visible = state.isLoading,
                    modifier = Modifier.fillMaxSize(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
@Preview
internal fun PasswordHistoryEntryScreenPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PasswordHistoryEntryContent(
                state = PasswordHistoryUiState(
                    items = persistentListOf(
                        PasswordHistoryItemUiState(
                            passwordHistoryEntryId = PasswordHistoryEntryId(0),
                            value = UIHiddenState.Concealed(
                                encrypted = "tttttt"
                            ),
                            date = "a date"
                        ),
                        PasswordHistoryItemUiState(
                            passwordHistoryEntryId = PasswordHistoryEntryId(1),
                            value = UIHiddenState.Concealed(
                                encrypted = "tttttt"
                            ),
                            date = "a date 2"
                        )
                    ),
                    isLoading = false
                ),
                onBackClick = {},
                onHideItem = {},
                onRevealItem = {},
                onMainThreeDotsMenuButtonClick = {},
                onThreeDotsMenuButtonClick = {},
                onCopyPassword = {}
            )
        }
    }
}

@Composable
@Preview
internal fun PasswordHistoryEntryScreenEmptyPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PasswordHistoryEntryContent(
                state = PasswordHistoryUiState(
                    items = persistentListOf(),
                    isLoading = false
                ),
                onBackClick = {},
                onHideItem = {},
                onRevealItem = {},
                onMainThreeDotsMenuButtonClick = {},
                onThreeDotsMenuButtonClick = {},
                onCopyPassword = {}
            )
        }
    }
}

@Composable
@Preview
internal fun PasswordHistoryEntryScreenLoadingPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PasswordHistoryEntryContent(
                state = PasswordHistoryUiState(
                    items = persistentListOf(),
                    isLoading = true
                ),
                onBackClick = {},
                onHideItem = {},
                onRevealItem = {},
                onMainThreeDotsMenuButtonClick = {},
                onThreeDotsMenuButtonClick = {},
                onCopyPassword = {}
            )
        }
    }
}
