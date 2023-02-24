package proton.android.pass.autofill.ui.autofill.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.extensions.toAutoFillItem
import proton.android.pass.autofill.ui.autofill.AutofillNavItem
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.featurecreateitem.impl.alias.AliasItem
import proton.android.pass.featurecreateitem.impl.alias.RESULT_CREATED_DRAFT_ALIAS
import proton.android.pass.featurecreateitem.impl.login.CreateLogin
import proton.android.pass.featurecreateitem.impl.login.InitialCreateLoginUiState
import proton.android.pass.featurecreateitem.impl.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalLifecycleComposeApi::class
)
fun NavGraphBuilder.createLoginGraph(
    appNavigator: AppNavigator,
    state: AutofillAppState,
    onAutofillItemReceived: (AutofillItem) -> Unit
) {
    composable(AutofillNavItem.CreateLogin) {
        val createdDraftAlias by appNavigator.navState<AliasItem>(RESULT_CREATED_DRAFT_ALIAS, null)
            .collectAsStateWithLifecycle()
        val primaryTotp by appNavigator.navState<String>(TOTP_NAV_PARAMETER_KEY, null)
            .collectAsStateWithLifecycle()

        val initialContents = InitialCreateLoginUiState(
            title = state.title,
            url = state.webDomain.value(),
            aliasItem = createdDraftAlias,
            packageInfoUi = state.packageInfoUi.takeIf { state.webDomain.isEmpty() },
            primaryTotp = primaryTotp
        )

        CreateLogin(
            initialContents = initialContents,
            onClose = { appNavigator.onBackClick() },
            onSuccess = {
                when (val autofillItem = it.toAutoFillItem()) {
                    None -> {}
                    is Some -> onAutofillItemReceived(autofillItem.value)
                }
            },
            onCreateAliasClick = { shareId, titleOption ->
                appNavigator.navigate(
                    AutofillNavItem.CreateAlias,
                    AutofillNavItem.CreateAlias.createNavRoute(
                        shareId = shareId,
                        isDraft = true,
                        title = titleOption
                    )
                )
            },
            onScanTotp = { appNavigator.navigate(AutofillNavItem.CameraTotp) }
        )
    }
}
