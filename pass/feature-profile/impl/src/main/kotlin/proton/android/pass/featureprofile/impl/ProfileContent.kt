package proton.android.pass.featureprofile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallStrong
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.uievents.value

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    state: ProfileUiState,
    onFingerprintClicked: (Boolean) -> Unit,
    onAutofillClicked: (Boolean) -> Unit,
    onAccountClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTipsClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onRateAppClick: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ItemSummary(itemSummaryUiState = state.itemSummaryUiState)
        Text(
            text = stringResource(R.string.profile_manage_profile),
            style = ProtonTheme.typography.defaultSmallStrong,
            color = PassTheme.colors.textNorm
        )
        if (state.fingerprintSection is FingerprintSectionState.Available) {
            FingerprintProfileSection(
                isChecked = state.fingerprintSection.enabled.value(),
                onClick = onFingerprintClicked
            )
        }
        if (state.autofillStatus is AutofillSupportedStatus.Supported) {
            AutofillProfileSection(
                isChecked = state.autofillStatus.status is AutofillStatus.EnabledByOurService,
                onClick = onAutofillClicked
            )
        }
        AccountProfileSection(onAccountClick = onAccountClick, onSettingsClick = onSettingsClick)
        HelpCenterProfileSection(
            onTipsClick = onTipsClick,
            onFeedbackClick = onFeedbackClick,
            onRateAppClick = onRateAppClick
        )
    }
}
