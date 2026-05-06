/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.composecomponents.impl.item

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.presentation.R as CoreR
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.passwords.PasswordChecksUiState
import proton.android.pass.composecomponents.impl.R

@Composable
fun PassPasswordChecksList(checks: PasswordChecksUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
    ) {
        PASSWORD_CHECK_ROWS.forEach { row ->
            PassPasswordCheckRow(
                labelRes = row.labelRes,
                isPassed = row.isPassed(checks)
            )
        }
    }
}

@Composable
private fun PassPasswordCheckRow(@StringRes labelRes: Int, isPassed: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Icon(
            modifier = Modifier.size(CHECK_ICON_SIZE),
            painter = painterResource(if (isPassed) PassedIconRes else FailedIconRes),
            tint = if (isPassed) PassTheme.colors.signalSuccess else PassTheme.colors.signalDanger,
            contentDescription = null
        )
        Text(
            text = stringResource(labelRes),
            style = checkTextStyle()
        )
    }
}

@Composable
private fun checkTextStyle(): TextStyle = TextStyle(
    fontSize = CHECK_FONT_SIZE,
    lineHeight = CHECK_LINE_HEIGHT,
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    color = PassTheme.colors.textNorm
)

private val CHECK_ICON_SIZE = 16.dp
private val CHECK_FONT_SIZE = 13.sp
private val CHECK_LINE_HEIGHT = 16.sp

private data class PasswordCheckRow(
    @StringRes val labelRes: Int,
    val isPassed: (PasswordChecksUiState) -> Boolean
)

private val PASSWORD_CHECK_ROWS: List<PasswordCheckRow> = listOf(
    PasswordCheckRow(R.string.password_check_min_length) { it.hasMinLength },
    PasswordCheckRow(R.string.password_check_lowercase) { it.hasLowercase },
    PasswordCheckRow(R.string.password_check_uppercase) { it.hasUppercase },
    PasswordCheckRow(R.string.password_check_no_consecutive) { it.hasNoRepeatedCharacters },
    PasswordCheckRow(R.string.password_check_no_common) { it.hasNoCommonPassword }
)

@DrawableRes
private val PassedIconRes: Int = CoreR.drawable.ic_proton_checkmark

@DrawableRes
private val FailedIconRes: Int = CoreR.drawable.ic_proton_cross_small

internal class PassPasswordChecksListPreviewProvider :
    PreviewParameterProvider<PasswordChecksUiState> {

    override val values: Sequence<PasswordChecksUiState> = sequenceOf(
        PasswordChecksUiState(
            hasMinLength = true,
            hasLowercase = true,
            hasUppercase = true,
            hasNoRepeatedCharacters = true,
            hasNoCommonPassword = true
        ),
        PasswordChecksUiState(
            hasMinLength = false,
            hasLowercase = true,
            hasUppercase = false,
            hasNoRepeatedCharacters = true,
            hasNoCommonPassword = true
        ),
        PasswordChecksUiState.Initial
    )
}

internal class ThemePassPasswordChecksListPreview :
    ThemePairPreviewProvider<PasswordChecksUiState>(
        PassPasswordChecksListPreviewProvider()
    )

@Preview
@Composable
internal fun PassPasswordChecksListPreview(
    @PreviewParameter(ThemePassPasswordChecksListPreview::class)
    input: Pair<Boolean, PasswordChecksUiState>
) {
    val (isDark, checks) = input
    PassTheme(isDark = isDark) {
        Surface {
            PassPasswordChecksList(checks = checks)
        }
    }
}
