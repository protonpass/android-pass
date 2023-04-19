package proton.android.pass.featureaccount.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.setting.SettingOption
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

@Composable
fun AccountInfo(modifier: Modifier = Modifier, state: AccountUiState) {
    Column(
        modifier = modifier.roundedContainerNorm()
    ) {
        SettingOption(
            text = state.email ?: "",
            label = stringResource(R.string.account_username_label),
            isLoading = state.isLoadingState.value()
        )

        Divider(color = PassTheme.colors.inputBorderNorm)

        if (state.plan != PlanSection.Hide) {
            SettingOption(
                text = state.plan.name(),
                label = stringResource(R.string.account_subscription_label),
                isLoading = state.plan.isLoading()
            )
        }
    }
}

@Preview
@Composable
fun AccountInfoPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            AccountInfo(
                state = AccountUiState(
                    "myemail@proton.me",
                    PlanSection.Data("Free"),
                    IsLoadingState.NotLoading
                )
            )
        }
    }
}
