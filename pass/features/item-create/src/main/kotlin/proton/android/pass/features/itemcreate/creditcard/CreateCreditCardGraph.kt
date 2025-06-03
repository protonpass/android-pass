package proton.android.pass.features.itemcreate.creditcard

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.common.KEY_VAULT_SELECTED
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath

object CreateCreditCardNavItem : NavItem(
    baseRoute = "creditcard/create/screen",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId)
) {
    fun createNavRoute(shareId: Option<ShareId> = None) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        val path = map.toPath()
        append(path)
    }
}

sealed interface CreateCreditCardNavigation : BaseCreditCardNavigation {
    @JvmInline
    value class ItemCreated(val itemUiModel: ItemUiModel) : CreateCreditCardNavigation

    @JvmInline
    value class SelectVault(val shareId: ShareId) : CreateCreditCardNavigation
}

fun NavGraphBuilder.createCreditCardGraph(canUseAttachments: Boolean, onNavigate: (BaseCreditCardNavigation) -> Unit) {
    composable(CreateCreditCardNavItem) { navBackStack ->
        val selectVault by navBackStack.savedStateHandle
            .getStateFlow<String?>(KEY_VAULT_SELECTED, null)
            .collectAsStateWithLifecycle()

        CreateCreditCardScreen(
            selectVault = selectVault.toOption().map { ShareId(it) }.value(),
            canUseAttachments = canUseAttachments,
            onNavigate = onNavigate
        )
    }
}

