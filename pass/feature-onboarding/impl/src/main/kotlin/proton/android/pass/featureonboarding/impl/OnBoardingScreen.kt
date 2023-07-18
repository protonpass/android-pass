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

package proton.android.pass.featureonboarding.impl

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.toClassHolder

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnBoardingViewModel = hiltViewModel(),
    onBoardingShown: () -> Unit
) {
    val onBoardingUiState by viewModel.onBoardingUiState.collectAsStateWithLifecycle()
    LaunchedEffect(onBoardingUiState.isCompleted) {
        if (onBoardingUiState.isCompleted) {
            onBoardingShown()
        }
    }
    val context = LocalContext.current
    OnBoardingContent(
        modifier = modifier.testTag(OnBoardingScreenTestTag.screen),
        uiState = onBoardingUiState,
        onMainButtonClick = { viewModel.onMainButtonClick(it, context.toClassHolder()) },
        onSkipButtonClick = viewModel::onSkipButtonClick,
        onSelectedPageChanged = viewModel::onSelectedPageChanged
    )
}

object OnBoardingScreenTestTag {
    const val screen = "OnBoardingScreen"
}
