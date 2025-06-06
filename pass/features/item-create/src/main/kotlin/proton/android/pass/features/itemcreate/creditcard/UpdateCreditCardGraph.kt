package proton.android.pass.features.itemcreate.creditcard

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.bottomsheets.customfield.customFieldBottomSheetGraph
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.customFieldNameDialogGraph
import proton.android.pass.features.itemcreate.totp.INDEX_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.createTotpGraph
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object EditCreditCardNavItem : NavItem(
    baseRoute = "creditcard/edit/screen",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) = "$baseRoute/${shareId.id}/${itemId.id}"
}

sealed interface UpdateCreditCardNavigation : BaseCreditCardNavigation {
    data class ItemUpdated(val shareId: ShareId, val itemId: ItemId) : UpdateCreditCardNavigation
}

fun NavGraphBuilder.updateCreditCardGraph(onNavigate: (BaseCreditCardNavigation) -> Unit) {
    composable(EditCreditCardNavItem) { navBackStack ->
        val navTotpUri by navBackStack.savedStateHandle
            .getStateFlow<String?>(TOTP_NAV_PARAMETER_KEY, null)
            .collectAsStateWithLifecycle()

        LaunchedEffect(navTotpUri) {
            navBackStack.savedStateHandle.remove<String?>(TOTP_NAV_PARAMETER_KEY)
        }

        val navTotpIndex by navBackStack.savedStateHandle
            .getStateFlow<Int?>(INDEX_NAV_PARAMETER_KEY, null)
            .collectAsStateWithLifecycle()

        LaunchedEffect(navTotpIndex) {
            navBackStack.savedStateHandle.remove<Int?>(INDEX_NAV_PARAMETER_KEY)
        }

        UpdateCreditCardScreen(
            navTotpUri = navTotpUri,
            navTotpIndex = navTotpIndex,
            onNavigate = onNavigate
        )
    }
    customFieldBottomSheetGraph(
        prefix = CustomFieldPrefix.UpdateCreditCard,
        onAddCustomFieldNavigate = { type, _ ->
            onNavigate(BaseCreditCardNavigation.CustomFieldTypeSelected(type))
        },
        onEditCustomFieldNavigate = { title: String, index: Int, _: Option<Int> ->
            onNavigate(BaseCreditCardNavigation.EditCustomField(title, index))
        },
        onRemoveCustomFieldNavigate = { onNavigate(BaseCreditCardNavigation.RemovedCustomField) },
        onDismissBottomsheet = { onNavigate(BaseCreditCardNavigation.DismissBottomsheet) }
    )
    customFieldNameDialogGraph(CustomFieldPrefix.UpdateCreditCard) {
        when (it) {
            is CustomFieldNameNavigation.CloseScreen -> {
                onNavigate(BaseCreditCardNavigation.CloseScreen)
            }
        }
    }
    createTotpGraph(
        prefix = CustomFieldPrefix.UpdateCreditCard,
        onSuccess = { totp, _, _, index ->
            val values = buildMap<String, Any> {
                put(TOTP_NAV_PARAMETER_KEY, totp)
                index?.let { put(INDEX_NAV_PARAMETER_KEY, it) }
            }
            onNavigate(BaseCreditCardNavigation.TotpSuccess(values))
        },
        onCloseTotp = { onNavigate(BaseCreditCardNavigation.TotpCancel) },
        onOpenImagePicker = { _, _, index ->
            onNavigate(BaseCreditCardNavigation.OpenImagePicker(index.toOption()))
        }
    )
}

