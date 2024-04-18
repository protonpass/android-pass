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

package proton.android.pass.features.security.center.report.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.loading.Loading
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.features.security.center.R

@Composable
internal fun ResolveButton(
    modifier: Modifier = Modifier,
    emailId: BreachEmailId,
    isLoading: Boolean,
    onUiEvent: (SecurityCenterReportUiEvent) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(PassTheme.colors.interactionNormMinor1)
            .height(48.dp)
            .clickable(onClick = { onUiEvent(SecurityCenterReportUiEvent.MarkAsResolvedClick(emailId)) }),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Loading()
        } else {
            Text(
                modifier = Modifier.padding(Spacing.medium),
                text = stringResource(R.string.security_center_email_report_mark_as_resolved),
                style = ProtonTheme.typography.body1Regular,
                color = PassTheme.colors.interactionNormMajor2
            )
        }
    }
}
