package proton.android.pass.autofill.ui.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.extensions.toAutoFillItem
import proton.android.pass.autofill.ui.autofill.navigation.SelectItem
import proton.android.pass.autofill.ui.autofill.navigation.selectItemGraph
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.featureauth.impl.authGraph
import proton.android.pass.featureitemcreate.impl.login.CreateLogin
import proton.android.pass.featureitemcreate.impl.login.InitialCreateLoginUiState
import proton.android.pass.featureitemcreate.impl.login.createLoginGraph
import proton.android.pass.featureitemcreate.impl.totp.CameraTotp
import proton.android.pass.featureitemcreate.impl.totp.PhotoPickerTotp
import proton.android.pass.featureitemcreate.impl.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.featureitemcreate.impl.totp.createTotpGraph
import proton.android.pass.navigation.api.AppNavigator

@Suppress("LongParameterList")
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.appGraph(
    appNavigator: AppNavigator,
    autofillAppState: AutofillAppState,
    selectedAutofillItem: AutofillItem?,
    onAutofillSuccess: (AutofillMappings) -> Unit,
    onAutofillCancel: () -> Unit,
    onAutofillItemReceived: (AutofillItem) -> Unit
) {
    authGraph(
        onNavigateBack = onAutofillCancel,
        onAuthSuccessful = {
            if (selectedAutofillItem != null) {
                onAutofillItemReceived(selectedAutofillItem)
            } else {
                appNavigator.navigate(SelectItem)
            }
        },
        onAuthDismissed = onAutofillCancel,
        onAuthFailed = onAutofillCancel
    )
    selectItemGraph(
        state = autofillAppState,
        onCreateLoginClicked = { appNavigator.navigate(CreateLogin) },
        onAutofillItemClicked = onAutofillSuccess,
        onAutofillCancel = onAutofillCancel
    )
    createLoginGraph(
        initialCreateLoginUiState = InitialCreateLoginUiState(
            title = autofillAppState.title,
            url = autofillAppState.webDomain.value(),
            aliasItem = null,
            packageInfoUi = autofillAppState.packageInfoUi.takeIf { autofillAppState.webDomain.isEmpty() },
        ),
        getPrimaryTotp = { appNavigator.navState<String>(TOTP_NAV_PARAMETER_KEY, null) },
        onClose = { appNavigator.onBackClick() },
        onSuccess = {
            when (val autofillItem = it.toAutoFillItem()) {
                None -> {}
                is Some -> onAutofillItemReceived(autofillItem.value)
            }
        },
        onScanTotp = { appNavigator.navigate(CameraTotp) }
    )
    createTotpGraph(
        onUriReceived = { totp -> appNavigator.navigateUpWithResult(TOTP_NAV_PARAMETER_KEY, totp) },
        onCloseTotp = { appNavigator.onBackClick() },
        onOpenImagePicker = {
            appNavigator.navigate(
                destination = PhotoPickerTotp,
                backDestination = CreateLogin
            )
        }
    )
}
