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

package proton.android.pass.featureitemdetail.impl.login

import androidx.annotation.DrawableRes
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.toPasswordAnnotatedString
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.common.HiddenContentRow
import proton.pass.domain.HiddenState
import me.proton.core.presentation.R as CoreR

private const val CHAR_AMOUNT = 12

@Composable
internal fun LoginPasswordRow(
    modifier: Modifier = Modifier,
    passwordHiddenState: HiddenState,
    label: String = stringResource(R.string.field_password),
    @DrawableRes iconRes: Int = CoreR.drawable.ic_proton_key,
    onTogglePasswordClick: () -> Unit,
    onCopyPasswordClick: () -> Unit
) {
    val (sectionContent, isContentVisible) = when (passwordHiddenState) {
        is HiddenState.Concealed -> "•".repeat(CHAR_AMOUNT) to false
        is HiddenState.Revealed -> passwordHiddenState.clearText to true
        is HiddenState.Empty -> "" to false
    }

    HiddenContentRow(
        modifier = modifier,
        isContentVisible = isContentVisible,
        sectionContent = sectionContent.toPasswordAnnotatedString(
            digitColor = ProtonTheme.colors.notificationError,
            symbolColor = ProtonTheme.colors.notificationSuccess,
            letterColor = ProtonTheme.colors.textNorm
        ),
        label = label,
        icon = {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = PassTheme.colors.loginInteractionNorm
            )
        },
        toggleIconBackground = PassTheme.colors.loginInteractionNormMinor1,
        toggleIconForeground = PassTheme.colors.loginInteractionNormMajor2,
        revealAction = stringResource(R.string.action_reveal_password),
        concealAction = stringResource(R.string.action_conceal_password),
        onRowClick = onCopyPasswordClick,
        onToggleClick = onTogglePasswordClick
    )
}
