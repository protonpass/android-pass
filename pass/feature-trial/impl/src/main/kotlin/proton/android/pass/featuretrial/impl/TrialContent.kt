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

package proton.android.pass.featuretrial.impl

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TrialContent(
    modifier: Modifier = Modifier,
    state: TrialUiState,
    onNavigate: (TrialNavigation) -> Unit,
    onLearnMore: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.trial),
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(52.dp))

        Text(
            text = stringResource(R.string.trial_title),
            style = PassTypography.hero
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.trial_subtitle),
            style = ProtonTheme.typography.defaultNorm
        )
        Spacer(modifier = Modifier.height(35.dp))

        TrialFeatures()

        Spacer(modifier = Modifier.height(28.dp))

        TrialGradientButton(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(R.string.trial_button_text),
            onClick = { onNavigate(TrialNavigation.Upgrade) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = pluralStringResource(
                R.plurals.trial_days_left,
                state.remainingTrialDays,
                state.remainingTrialDays
            ),
            style = PassTypography.body3Regular
        )

        Text(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onLearnMore()
                }
                .padding(8.dp),
            text = stringResource(R.string.trial_learn_more),
            color = PassTheme.colors.interactionNormMajor2,
            style = PassTypography.body3Regular.copy(textDecoration = TextDecoration.Underline)
        )
    }
}

@Preview
@Composable
fun TrialContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            TrialContent(
                state = TrialUiState(remainingTrialDays = 1),
                onNavigate = {},
                onLearnMore = {}
            )
        }
    }
}
