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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.topbar.IconTopAppBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.features.report.R
import proton.android.pass.features.report.navigation.ReportNavContentEvent
import proton.android.pass.features.report.presentation.ReportFormData
import proton.android.pass.features.report.presentation.ReportState

@Composable
internal fun ReportContent(
    modifier: Modifier = Modifier,
    formState: ReportFormData,
    state: ReportState,
    onEvent: (ReportNavContentEvent) -> Unit
) {
    val pagerState: PagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val backHandler = {
        if (pagerState.canScrollBackward) {
            if (pagerState.currentPage == ReportPage.Tips.value) {
                onEvent(ReportNavContentEvent.CancelReason)
            }

            when (state.reportReasonOption.value()) {
                ReportReason.Other -> {
                    onEvent(ReportNavContentEvent.CancelReason)
                    navigateToPage(scope, pagerState, ReportPage.Categories)
                }

                else -> {
                    scope.launch { pagerState.scrollToPage(pagerState.currentPage - 1) }
                }
            }
        } else {
            onEvent(ReportNavContentEvent.Close)
        }
    }
    BackHandler { backHandler() }
    LaunchedEffect(state.reportReasonOption) {
        if (state.reportReasonOption is Some) {
            when (state.reportReasonOption.value) {
                ReportReason.Autofill,
                ReportReason.Sharing,
                ReportReason.Sync,
                ReportReason.Passkeys -> navigateToPage(scope, pagerState, ReportPage.Tips)

                ReportReason.Other -> navigateToPage(scope, pagerState, ReportPage.Form)
            }
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            IconTopAppBar(
                backgroundColor = PassTheme.colors.itemDetailBackground,
                title = stringResource(R.string.report_toolbar_title),
                navigationIcon = {
                    BackArrowCircleIconButton(
                        modifier = Modifier.padding(Spacing.mediumSmall, Spacing.extraSmall),
                        color = PassTheme.colors.interactionNorm,
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        onUpClick = { backHandler() }
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
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = (pagerState.currentPage.toFloat() + 1) / ReportPage.entries.count(),
                color = PassTheme.colors.interactionNorm
            )
            Text.Body1Regular(
                modifier = Modifier.padding(Spacing.medium),
                text = stringResource(R.string.report_steps, pagerState.currentPage + 1, ReportPage.entries.count())
            )
            Spacer(modifier = Modifier.height(Spacing.medium))
            HorizontalPager(
                modifier = Modifier,
                state = pagerState,
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    ReportPage.Categories.value -> ReportCategoriesPage(
                        onReasonClicked = { onEvent(ReportNavContentEvent.OnReasonChange(it)) }
                    )

                    ReportPage.Tips.value -> ReportTipsPage(
                        passkeySupportOption = state.passkeySupportOption,
                        reportReasonOption = state.reportReasonOption,
                        onEvent = onEvent,
                        onReportIssue = { navigateToPage(scope, pagerState, ReportPage.Form) }
                    )

                    ReportPage.Form.value -> ReportFormPage(
                        state = state,
                        formState = formState,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}

internal enum class ReportPage(internal val value: Int) {
    Categories(value = 0),
    Tips(value = 1),
    Form(value = 2)
}

@OptIn(ExperimentalFoundationApi::class)
internal fun navigateToPage(
    scope: CoroutineScope,
    pagerState: PagerState,
    reportPage: ReportPage
) {
    scope.launch { pagerState.scrollToPage(reportPage.value) }
}
