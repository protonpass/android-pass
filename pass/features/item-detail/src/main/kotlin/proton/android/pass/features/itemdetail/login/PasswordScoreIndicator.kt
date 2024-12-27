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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import proton.android.pass.commonrust.api.PasswordScore
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.features.itemdetail.R

@Composable
fun PasswordScoreIndicator(modifier: Modifier = Modifier, passwordScore: PasswordScore) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
    ) {
        ShieldIcon(passwordScore = passwordScore)
        ScoreText(passwordScore = passwordScore)
    }
}

@Composable
fun ShieldIcon(modifier: Modifier = Modifier, passwordScore: PasswordScore) {
    when (passwordScore) {
        PasswordScore.VULNERABLE -> Icon(
            modifier = modifier,
            painter = painterResource(R.drawable.ic_shield_bad),
            tint = PassTheme.colors.signalDanger,
            contentDescription = null
        )

        PasswordScore.WEAK -> Icon(
            modifier = modifier,
            painter = painterResource(R.drawable.ic_shield_warning),
            tint = PassTheme.colors.signalWarning,
            contentDescription = null
        )

        PasswordScore.STRONG -> Icon(
            modifier = modifier,
            painter = painterResource(R.drawable.ic_shield_check),
            tint = PassTheme.colors.signalSuccess,
            contentDescription = null
        )
    }
}

@Composable
fun ScoreText(modifier: Modifier = Modifier, passwordScore: PasswordScore) {
    when (passwordScore) {
        PasswordScore.VULNERABLE -> Text(
            modifier = modifier,
            text = stringResource(R.string.password_score_vulnerable),
            style = ProtonTheme.typography.captionNorm
        )

        PasswordScore.WEAK -> Text(
            modifier = modifier,
            text = stringResource(R.string.password_score_weak),
            style = ProtonTheme.typography.captionNorm
        )

        PasswordScore.STRONG -> Text(
            modifier = modifier,
            text = stringResource(R.string.password_score_strong),
            style = ProtonTheme.typography.captionNorm
        )
    }
}

class ThemeAndPasswordScoreProvider :
    ThemePairPreviewProvider<PasswordScore>(PasswordScorePreviewProvider())

class PasswordScorePreviewProvider : PreviewParameterProvider<PasswordScore> {
    override val values: Sequence<PasswordScore>
        get() = PasswordScore.entries.asSequence()
}

@Preview
@Composable
fun PasswordScoreIndicatorPreview(
    @PreviewParameter(ThemeAndPasswordScoreProvider::class) input: Pair<Boolean, PasswordScore>
) {
    PassTheme(isDark = input.first) {
        Surface {
            PasswordScoreIndicator(passwordScore = input.second)
        }
    }
}
