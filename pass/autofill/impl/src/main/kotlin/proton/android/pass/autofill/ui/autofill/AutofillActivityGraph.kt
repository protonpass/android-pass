package proton.android.pass.autofill.ui.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.featuresearchoptions.impl.SortingBottomsheet
import proton.android.featuresearchoptions.impl.sortingGraph
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.extensions.CreatedAlias
import proton.android.pass.autofill.extensions.toAutoFillItem
import proton.android.pass.autofill.extensions.toAutofillItem
import proton.android.pass.autofill.ui.autofill.navigation.SelectItem
import proton.android.pass.autofill.ui.autofill.navigation.SelectItemNavigation
import proton.android.pass.autofill.ui.autofill.navigation.selectItemGraph
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.featureauth.impl.AuthNavigation
import proton.android.pass.featureauth.impl.authGraph
import proton.android.pass.featureitemcreate.impl.alias.CreateAlias
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasBottomSheet
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasNavigation
import proton.android.pass.featureitemcreate.impl.alias.createAliasGraph
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomSheetMode
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomsheet
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.CreateItemBottomsheetNavigation
import proton.android.pass.featureitemcreate.impl.bottomsheets.createitem.bottomsheetCreateItemGraph
import proton.android.pass.featureitemcreate.impl.login.BaseLoginNavigation
import proton.android.pass.featureitemcreate.impl.login.CreateLogin
import proton.android.pass.featureitemcreate.impl.login.GenerateLoginPasswordBottomsheet
import proton.android.pass.featureitemcreate.impl.login.InitialCreateLoginUiState
import proton.android.pass.featureitemcreate.impl.login.createLoginGraph
import proton.android.pass.featureitemcreate.impl.login.generatePasswordGraph
import proton.android.pass.featureitemcreate.impl.totp.CameraTotp
import proton.android.pass.featureitemcreate.impl.totp.PhotoPickerTotp
import proton.android.pass.featureitemcreate.impl.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.featureitemcreate.impl.totp.createTotpGraph
import proton.android.pass.navigation.api.AppNavigator

@Suppress("LongParameterList", "LongMethod", "ComplexMethod")
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalComposeUiApi
fun NavGraphBuilder.autofillActivityGraph(
    appNavigator: AppNavigator,
    autofillAppState: AutofillAppState,
    selectedAutofillItem: AutofillItem?,
    onAutofillSuccess: (AutofillMappings) -> Unit,
    onAutofillCancel: () -> Unit,
    onAutofillItemReceived: (AutofillItem) -> Unit,
    dismissBottomSheet: (() -> Unit) -> Unit
) {
    authGraph(
        navigation = {
            when (it) {
                AuthNavigation.Back -> {
                    onAutofillCancel()
                }

                AuthNavigation.Success -> {
                    if (selectedAutofillItem != null) {
                        onAutofillItemReceived(selectedAutofillItem)
                    } else {
                        appNavigator.navigate(SelectItem)
                    }
                }

                AuthNavigation.Dismissed -> {
                    onAutofillCancel()
                }

                AuthNavigation.Failed -> {
                    onAutofillCancel()
                }
            }
        }
    )
    selectItemGraph(
        state = autofillAppState,
        onNavigate = {
            when (it) {
                SelectItemNavigation.AddItem -> {
                    appNavigator.navigate(CreateItemBottomsheet)
                }
                SelectItemNavigation.Cancel -> onAutofillCancel()
                is SelectItemNavigation.ItemSelected -> onAutofillSuccess(it.autofillMappings)
                is SelectItemNavigation.SortingBottomsheet ->
                    appNavigator.navigate(
                        SortingBottomsheet,
                        SortingBottomsheet.createNavRoute(it.searchSortingType)
                    )
            }
        }
    )
    sortingGraph { appNavigator.onBackClick() }
    createLoginGraph(
        initialCreateLoginUiState = InitialCreateLoginUiState(
            title = autofillAppState.title,
            url = autofillAppState.webDomain.value(),
            aliasItem = null,
            packageInfoUi = autofillAppState.packageInfoUi.takeIf { autofillAppState.webDomain.isEmpty() },
        ),
        getPrimaryTotp = { appNavigator.navState<String>(TOTP_NAV_PARAMETER_KEY, null) },
        onNavigate = {
            when (it) {
                BaseLoginNavigation.Close -> appNavigator.onBackClick()
                is BaseLoginNavigation.CreateAlias -> appNavigator.navigate(
                    destination = CreateAliasBottomSheet,
                    route = CreateAliasBottomSheet.createNavRoute(it.shareId, it.title)
                )

                BaseLoginNavigation.GeneratePassword ->
                    appNavigator.navigate(GenerateLoginPasswordBottomsheet)

                is BaseLoginNavigation.LoginCreated -> when (
                    val autofillItem = it.itemUiModel.toAutoFillItem()
                ) {
                    None -> {}
                    is Some -> onAutofillItemReceived(autofillItem.value)
                }

                is BaseLoginNavigation.LoginUpdated -> {}
                BaseLoginNavigation.ScanTotp -> appNavigator.navigate(CameraTotp)
                BaseLoginNavigation.Upgrade -> {}
            }
        }
    )

    generatePasswordGraph(dismissBottomSheet = dismissBottomSheet)
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
    createAliasGraph(
        onNavigate = {
            when (it) {
                CreateAliasNavigation.Close -> appNavigator.onBackClick()
                is CreateAliasNavigation.CreatedFromBottomsheet -> {
                    dismissBottomSheet {
                        appNavigator.onBackClick()
                    }
                }
                is CreateAliasNavigation.Created -> {
                    val created = CreatedAlias(it.shareId, it.itemId, it.alias)
                    onAutofillItemReceived(created.toAutofillItem())
                }
            }
        }
    )
    bottomsheetCreateItemGraph(
        mode = CreateItemBottomSheetMode.Autofill,
        onNavigate = {
            when (it) {
                is CreateItemBottomsheetNavigation.CreateAlias -> {
                    appNavigator.navigate(
                        CreateAlias,
                        CreateAlias.createNavRoute(it.shareId)
                    )
                }
                is CreateItemBottomsheetNavigation.CreateLogin -> {
                    appNavigator.navigate(
                        CreateLogin,
                        CreateLogin.createNavRoute(it.shareId)
                    )
                }
                else -> {}
            }
        }
    )
}
