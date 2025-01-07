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

package proton.android.pass.features.itemdetail.login

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonrust.api.PasswordScore
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.toPasswordAnnotatedString
import proton.android.pass.domain.HiddenState
import proton.android.pass.features.itemdetail.R
import proton.android.pass.features.itemdetail.common.HiddenContentRow
import me.proton.core.presentation.R as CoreR

@Composable
internal fun LoginPasswordRow(
    modifier: Modifier = Modifier,
    passwordHiddenState: HiddenState,
    passwordScore: PasswordScore?,
    label: String = stringResource(R.string.field_password),
    @DrawableRes iconRes: Int = CoreR.drawable.ic_proton_key,
    onTogglePasswordClick: () -> Unit,
    onCopyPasswordClick: () -> Unit
) {
    val (sectionContent, isContentVisible) = when (passwordHiddenState) {
        is HiddenState.Concealed -> AnnotatedString("•".repeat(PASSWORD_CHAR_AMOUNT)) to false
        is HiddenState.Revealed -> passwordHiddenState.clearText.toPasswordAnnotatedString(
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
        label = label,
        textStyle = ProtonTheme.typography.defaultNorm.copy(fontFamily = FontFamily.Monospace),
        icon = {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = PassTheme.colors.loginInteractionNorm
            )
        },
        middleSection = {
            passwordScore?.let { score ->
                PasswordScoreIndicator(modifier = Modifier.padding(8.dp), passwordScore = score)
            }
        },
        toggleIconBackground = PassTheme.colors.loginInteractionNormMinor1,
        toggleIconForeground = PassTheme.colors.loginInteractionNormMajor2,
        revealAction = stringResource(R.string.action_reveal_password),
        concealAction = stringResource(R.string.action_conceal_password),
        onRowClick = onCopyPasswordClick,
        onToggleClick = onTogglePasswordClick
    )
}
