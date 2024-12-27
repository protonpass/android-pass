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

package proton.android.pass.features.itemdetail.login.customfield

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.toPasswordAnnotatedString
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.features.itemdetail.R
import proton.android.pass.features.itemdetail.common.HiddenContentRow
import proton.android.pass.features.itemdetail.login.CustomFieldUiContent
import proton.android.pass.features.itemdetail.login.HIDDEN_FIELD_CHAR_AMOUNT
import proton.android.pass.domain.HiddenState
import me.proton.core.presentation.R as CoreR

@Composable
fun CustomFieldHidden(
    modifier: Modifier = Modifier,
    entry: CustomFieldUiContent.Hidden,
    onToggleVisibility: () -> Unit,
    onCopyValue: () -> Unit
) {
    RoundedCornersColumn(modifier) {
        val (sectionContent, isContentVisible) = when (entry.content) {
            is HiddenState.Concealed -> AnnotatedString("•".repeat(HIDDEN_FIELD_CHAR_AMOUNT)) to false
            is HiddenState.Revealed -> entry.content.clearText.toPasswordAnnotatedString(
                digitColor = ProtonTheme.colors.notificationError,
                symbolColor = ProtonTheme.colors.notificationSuccess,
                letterColor = ProtonTheme.colors.textNorm
            ) to true

            is HiddenState.Empty -> AnnotatedString("") to false
        }

        HiddenContentRow(
            modifier = modifier,
            isContentVisible = isContentVisible,
            sectionContent = sectionContent,
            label = entry.label,
            icon = {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_proton_eye_slash),
                    contentDescription = null,
                    tint = PassTheme.colors.loginInteractionNorm
                )
            },
            toggleIconBackground = PassTheme.colors.loginInteractionNormMinor1,
            toggleIconForeground = PassTheme.colors.loginInteractionNormMajor2,
            revealAction = stringResource(R.string.action_reveal_password),
            concealAction = stringResource(R.string.action_conceal_password),
            onRowClick = onCopyValue,
            onToggleClick = onToggleVisibility
        )
    }
}
