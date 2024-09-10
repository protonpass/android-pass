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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.report.navigation.ReportNavContentEvent
import proton.android.pass.features.report.presentation.ReportFormData
import proton.android.pass.features.report.presentation.ReportState

@Composable
internal fun ReportFormPage(
    modifier: Modifier = Modifier,
    formState: ReportFormData,
    state: ReportState,
    onEvent: (ReportNavContentEvent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        EmailField(
            modifier = Modifier.padding(horizontal = Spacing.medium),
            value = formState.email,
            enabled = !state.isLoading,
            error = state.emailErrors.firstOrNull(),
            onChange = { onEvent(ReportNavContentEvent.OnEmailChange(it)) }
        )
        DescriptionField(
            modifier = Modifier
                .padding(horizontal = Spacing.medium)
                .defaultMinSize(minHeight = 150.dp),
            value = formState.description,
            enabled = !state.isLoading,
            error = state.descriptionErrors.firstOrNull(),
            onChange = { onEvent(ReportNavContentEvent.OnDescriptionChange(it)) }
        )
        ImageAttach(images = formState.extraFiles, onEvent = onEvent)
        Spacer(Modifier.weight(1f))
        SendLogs(formState = formState, onEvent = onEvent)
        Button.Circular(
            modifier = Modifier
                .padding(Spacing.medium)
                .fillMaxWidth(),
            color = PassTheme.colors.loginInteractionNormMajor1,
            contentPadding = PaddingValues(Spacing.mediumSmall),
            elevation = ButtonDefaults.elevation(0.dp),
            onClick = { onEvent(ReportNavContentEvent.SubmitReport) }
        ) {
            Text.Body1Regular("Send report")
        }
    }
}

@Preview
@Composable
fun ReportFormPagePreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ReportFormPage(
                formState = ReportFormData(),
                state = ReportState.Initial,
                onEvent = {}
            )
        }
    }
}
