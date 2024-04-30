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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.features.security.center.R

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun ExposedData(modifier: Modifier = Modifier, breachEmail: BreachEmail) {
    Column(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        breachEmail.exposedData.takeIf { it.isNotEmpty() }?.let { dataList ->
            Text(
                text = stringResource(R.string.security_center_report_detail_your_exposed_information),
                style = ProtonTheme.typography.body1Medium
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                dataList.forEach {
                    Text(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PassTheme.colors.signalDanger)
                            .padding(Spacing.small),
                        text = it,
                        style = PassTheme.typography.body3Norm(),
                        color = PassTheme.colors.textInvert
                    )
                }
            }
        }
    }
}
