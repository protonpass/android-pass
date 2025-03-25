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

package proton.android.pass.features.security.center.breachdetail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.features.security.center.breachdetail.presentation.SecurityCenterBreachDetailState

@Composable
internal fun SecurityCenterBreachDetailBSContent(
    modifier: Modifier = Modifier,
    state: SecurityCenterBreachDetailState,
    onOpenUrl: (String) -> Unit
) = with(state) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = PassTheme.colors.backgroundNorm)
            .bottomSheet(horizontalPadding = Spacing.medium)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.mediumLarge)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            breachEmail?.let { breachEmail ->
                BreachDetailHeader(
                    isResolved = breachEmail.isResolved,
                    name = breachEmail.name,
                    publishedAt = breachEmail.publishedAt
                )

                if (breachEmail.exposedData.isNotEmpty()) {
                    ExposedData(exposedDataList = breachEmail.exposedData)
                }

                breachEmail.passwordLastChars
                    ?.takeIf { it.isNotBlank() }
                    ?.let { passwordLastChars ->
                        Details(passwordLastChars = passwordLastChars)
                    }

                if (breachEmail.actions.isNotEmpty()) {
                    RecommendedActions(
                        breachActions = breachEmail.actions,
                        onOpenUrl = onOpenUrl
                    )
                }

                Footer(
                    name = breachEmail.name,
                    onOpenUrl = onOpenUrl
                )
            }
        }
    }
}
