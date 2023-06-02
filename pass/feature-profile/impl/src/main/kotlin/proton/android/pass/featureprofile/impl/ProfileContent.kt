package proton.android.pass.featureprofile.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import me.proton.core.compose.theme.defaultSmallStrongNorm
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.composecomponents.impl.bottombar.BottomBar
import proton.android.pass.composecomponents.impl.bottombar.BottomBarSelected
import proton.android.pass.composecomponents.impl.uievents.value

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    state: ProfileUiState,
    onListClick: () -> Unit,
    onCreateItemClick: () -> Unit,
    onFingerprintClicked: (Boolean) -> Unit,
    onAppLockClick: () -> Unit,
    onAutofillClicked: (Boolean) -> Unit,
    onAccountClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onImportExportClick: () -> Unit,
    onRateAppClick: () -> Unit,
    onCopyAppVersionClick: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ProtonTopAppBar(
                backgroundColor = PassTheme.colors.backgroundStrong,
                title = {
                    Text(
                        text = stringResource(R.string.profile_screen_title),
                        style = PassTypography.hero
                    )
                }
            )
        },
        bottomBar = {
            BottomBar(
                bottomBarSelected = BottomBarSelected.Profile,
                accountType = state.accountType,
                onListClick = onListClick,
                onCreateClick = onCreateItemClick,
                onProfileClick = {}
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .background(PassTheme.colors.backgroundStrong)
                .padding(padding),
        ) {
            ItemSummary(
                modifier = Modifier.padding(0.dp, 16.dp),
                itemSummaryUiState = state.itemSummaryUiState
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_manage_profile),
                    style = ProtonTheme.typography.defaultSmallStrongNorm,
                    color = PassTheme.colors.textNorm
                )
                if (state.fingerprintSection is FingerprintSectionState.Available) {
                    FingerprintProfileSection(
                        isFingerprintEnabled = state.fingerprintSection.enabled.value(),
                        onFingerprintToggle = onFingerprintClicked,
                        onAppLockClick = onAppLockClick
                    )
                }
                if (state.autofillStatus is AutofillSupportedStatus.Supported) {
                    AutofillProfileSection(
                        isChecked = state.autofillStatus.status is AutofillStatus.EnabledByOurService,
                        onClick = onAutofillClicked
                    )
                }
                AccountProfileSection(
                    onAccountClick = onAccountClick,
                    onSettingsClick = onSettingsClick
                )
                HelpCenterProfileSection(
                    onFeedbackClick = onFeedbackClick,
                    onImportExportClick = onImportExportClick,
                    onRateAppClick = onRateAppClick
                )
                Box(
                    modifier = Modifier
                        .clickable { onCopyAppVersionClick() }
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.appVersion,
                        style = ProtonTheme.typography.captionWeak
                    )
                }
            }
        }
    }
}
