package proton.android.pass.autofill.ui.autosave

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable
import proton.android.pass.autofill.entities.usernamePassword
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.featureauth.impl.AUTH_SCREEN_ROUTE
import proton.android.pass.featureauth.impl.AuthNavigation
import proton.android.pass.featureauth.impl.AuthScreen
import proton.android.pass.featureitemcreate.impl.common.KEY_VAULT_SELECTED
import proton.android.pass.featureitemcreate.impl.login.BaseLoginNavigation
import proton.android.pass.featureitemcreate.impl.login.CreateLogin
import proton.android.pass.featureitemcreate.impl.login.InitialCreateLoginUiState
import proton.android.pass.featureitemcreate.impl.login.createLoginGraph
import proton.android.pass.featureitemcreate.impl.totp.CameraTotp
import proton.android.pass.featureitemcreate.impl.totp.PhotoPickerTotp
import proton.android.pass.featureitemcreate.impl.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.featureitemcreate.impl.totp.createTotpGraph
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheet
import proton.android.pass.featurepassword.impl.GeneratePasswordBottomsheetModeValue
import proton.android.pass.featurepassword.impl.GeneratePasswordNavigation
import proton.android.pass.featurepassword.impl.dialog.mode.PasswordModeDialog
import proton.android.pass.featurepassword.impl.dialog.separator.WordSeparatorDialog
import proton.android.pass.featurepassword.impl.generatePasswordBottomsheetGraph
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.featurevault.impl.bottomsheet.select.SelectVaultBottomsheet
import proton.android.pass.featurevault.impl.vaultGraph
import proton.android.pass.navigation.api.AppNavigator

@Suppress("ComplexMethod", "LongMethod")
@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.autosaveActivityGraph(
    appNavigator: AppNavigator,
    arguments: AutoSaveArguments,
    onAutoSaveCancel: () -> Unit,
    onAutoSaveSuccess: () -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    composable(AUTH_SCREEN_ROUTE) {
        AuthScreen(
            navigation = {
                when (it) {
                    AuthNavigation.Back -> onAutoSaveCancel()
                    AuthNavigation.Success -> appNavigator.navigate(CreateLogin)
                    AuthNavigation.Dismissed -> onAutoSaveCancel()
                    AuthNavigation.Failed -> onAutoSaveCancel()
                }
            }
        )
    }
    createLoginGraph(
        initialCreateLoginUiState = getInitialState(arguments),
        showCreateAliasButton = false,
        getPrimaryTotp = { appNavigator.navState<String>(TOTP_NAV_PARAMETER_KEY, null) },
        onNavigate = {
            when (it) {
                BaseLoginNavigation.Close -> onAutoSaveCancel()
                is BaseLoginNavigation.CreateAlias -> {}
                BaseLoginNavigation.GeneratePassword ->
                    appNavigator.navigate(
                        destination = GeneratePasswordBottomsheet,
                        route = GeneratePasswordBottomsheet.buildRoute(
                            mode = GeneratePasswordBottomsheetModeValue.CancelConfirm
                        )
                    )

                is BaseLoginNavigation.LoginCreated -> onAutoSaveSuccess()
                is BaseLoginNavigation.LoginUpdated -> {}
                BaseLoginNavigation.ScanTotp -> appNavigator.navigate(CameraTotp)
                BaseLoginNavigation.Upgrade -> {}

                // Alias generation in Autosave is not supported
                is BaseLoginNavigation.AliasOptions -> {}
                BaseLoginNavigation.DeleteAlias -> {}
                is BaseLoginNavigation.EditAlias -> {}

                is BaseLoginNavigation.SelectVault -> {
                    appNavigator.navigate(
                        destination = SelectVaultBottomsheet,
                        route = SelectVaultBottomsheet.createNavRoute(it.shareId.toOption())
                    )
                }
            }
        }
    )
    generatePasswordBottomsheetGraph(
        onNavigate = {
            when (it) {
                GeneratePasswordNavigation.CloseDialog -> appNavigator.onBackClick()
                GeneratePasswordNavigation.DismissBottomsheet -> dismissBottomSheet {
                    appNavigator.onBackClick()
                }
                GeneratePasswordNavigation.OnSelectWordSeparator -> appNavigator.navigate(
                    destination = WordSeparatorDialog
                )
                GeneratePasswordNavigation.OnSelectPasswordMode -> appNavigator.navigate(
                    destination = PasswordModeDialog
                )
            }
        }
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

    vaultGraph(
        onNavigate = {
            when (it) {
                VaultNavigation.Close -> appNavigator.onBackClick()
                VaultNavigation.Upgrade -> {
                    throw IllegalStateException("Do not forget to implement this one")
                }
                is VaultNavigation.VaultSelected -> {
                    dismissBottomSheet {
                        appNavigator.navigateUpWithResult(KEY_VAULT_SELECTED, it.shareId.id)
                    }
                }
            }
        }
    )
}

private fun getInitialState(arguments: AutoSaveArguments): InitialCreateLoginUiState {
    val saveInformation = arguments.saveInformation
    val (username, password) = saveInformation.usernamePassword()
    val packageInfoUi = arguments.linkedAppInfo?.let {
        PackageInfoUi(packageName = it.packageName, appName = it.appName)
    }
    return InitialCreateLoginUiState(
        username = username,
        password = password,
        url = arguments.website,
        title = arguments.title,
        packageInfoUi = packageInfoUi
    )
}
