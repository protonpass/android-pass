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

package proton.android.pass.composecomponents.impl.bottombar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import me.proton.core.presentation.R as CoreR

@Composable
fun ProfileBottomBarIcon(
    modifier: Modifier = Modifier,
    accountType: AccountType
) {
    Box(modifier = modifier.size(40.dp)) {
        when (accountType) {
            AccountType.Free -> {}
            AccountType.Unlimited -> {
                Icon(
                    modifier = Modifier.align(Alignment.TopEnd),
                    painter = painterResource(R.drawable.account_unlimited_indicator),
                    contentDescription = stringResource(R.string.bottom_bar_profile_icon_content_description),
                    tint = Color.Unspecified
                )
            }

            AccountType.Trial -> {
                Icon(
                    modifier = Modifier.align(Alignment.TopEnd),
                    painter = painterResource(R.drawable.account_trial_indicator),
                    contentDescription = stringResource(R.string.bottom_bar_profile_icon_content_description),
                    tint = Color.Unspecified
                )
            }
        }

        Icon(
            modifier = Modifier.align(Alignment.Center),
            painter = painterResource(CoreR.drawable.ic_proton_user),
            contentDescription = stringResource(R.string.bottom_bar_profile_icon_content_description)
        )
    }
}

@Preview
@Composable
fun ProfileBottomBarIconFreePreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            ProfileBottomBarIcon(accountType = AccountType.Free)
        }
    }
}

@Preview
@Composable
fun ProfileBottomBarIconTrialPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            ProfileBottomBarIcon(accountType = AccountType.Trial)
        }
    }
}

@Preview
@Composable
fun ProfileBottomBarIconUnlimitedPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            ProfileBottomBarIcon(accountType = AccountType.Unlimited)
        }
    }
}
