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
