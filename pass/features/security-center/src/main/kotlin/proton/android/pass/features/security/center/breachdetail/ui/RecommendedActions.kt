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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.breach.BreachAction
import proton.android.pass.features.security.center.R

@Composable
internal fun RecommendedActions(
    modifier: Modifier = Modifier,
    breachActions: List<BreachAction>,
    onOpenUrl: (String) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        Text.Body1Medium(
            text = stringResource(R.string.security_center_report_detail_recommended_actions)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            breachActions.forEach { breachAction ->
                RecommendedAction(
                    text = breachAction.name,
                    icon = breachAction.code.toResource(),
                    url = breachAction.url,
                    onClick = {
                        breachAction.url?.let { onOpenUrl(it) }
                    }
                )
            }
        }
    }
}
