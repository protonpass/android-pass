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

package proton.android.pass.features.report.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.topbar.iconbutton.CrossBackCircleIconButton
import proton.android.pass.features.report.navigation.ReportNavContentEvent
import proton.android.pass.features.report.presentation.ReportFormData
import proton.android.pass.features.report.presentation.ReportState

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ReportContent(
    modifier: Modifier = Modifier,
    formState: ReportFormData,
    state: ReportState,
    onEvent: (ReportNavContentEvent) -> Unit
) {
    val pagerState: PagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    BackHandler { onEvent(ReportNavContentEvent.Close) }
    LaunchedEffect(state.reportReasonOption) {
        if (state.reportReasonOption is Some) {
            when (state.reportReasonOption.value) {
                ReportReason.Autofill,
                ReportReason.Sharing,
                ReportReason.Sync,
                ReportReason.Passkeys -> navigateToTipsPage(scope, pagerState)

                ReportReason.Other -> navigateToFormPage(scope, pagerState)
            }
        } else {
            navigateToReasonsPage(scope, pagerState)
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                backgroundColor = PassTheme.colors.itemDetailBackground,
                title = { Text.Body1Regular("Report an issue") },
                navigationIcon = {
                    CrossBackCircleIconButton(
                        modifier = Modifier.padding(Spacing.mediumSmall, Spacing.extraSmall),
                        color = PassTheme.colors.interactionNorm,
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onUpClick = { onEvent(ReportNavContentEvent.Close) }
                    )
                }
            )
        }
    ) { innerPaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPaddingValues)
        ) {
            Spacer(modifier = Modifier.height(Spacing.medium))
            HorizontalPager(
                modifier = Modifier,
                state = pagerState,
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> ReportCategoriesPage(
                        onReasonClicked = { onEvent(ReportNavContentEvent.OnReasonChange(it)) }
                    )

                    1 -> ReportTipsPage(
                        reportReasonOption = state.reportReasonOption,
                        onEvent = onEvent,
                        onReportIssue = { navigateToFormPage(scope, pagerState) }
                    )

                    2 -> ReportFormPage(
                        state = state,
                        formState = formState,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun navigateToReasonsPage(scope: CoroutineScope, pagerState: PagerState) {
    scope.launch { pagerState.scrollToPage(0) }
}

@OptIn(ExperimentalFoundationApi::class)
fun navigateToTipsPage(scope: CoroutineScope, pagerState: PagerState) {
    scope.launch { pagerState.scrollToPage(1) }
}

@OptIn(ExperimentalFoundationApi::class)
fun navigateToFormPage(scope: CoroutineScope, pagerState: PagerState) {
    scope.launch { pagerState.scrollToPage(2) }
}

@Preview
@Composable
fun ReportContentPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ReportContent(
                formState = ReportFormData(),
                state = ReportState.Initial.copy(reportReasonOption = Some(ReportReason.Autofill)),
                onEvent = {}
            )
        }
    }
}
