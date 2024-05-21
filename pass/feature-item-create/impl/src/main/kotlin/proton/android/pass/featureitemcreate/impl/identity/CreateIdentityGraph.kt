package proton.android.pass.featureitemcreate.impl.identity

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.common.KEY_VAULT_SELECTED
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath

object CreateIdentity : NavItem(
    baseRoute = "identity/create/screen",
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

sealed interface CreateIdentityNavigation : BaseIdentityNavigation {
    @JvmInline
    value class ItemCreated(val itemUiModel: ItemUiModel) : CreateIdentityNavigation

    @JvmInline
    value class SelectVault(val shareId: ShareId) : CreateIdentityNavigation
}

fun NavGraphBuilder.createIdentityGraph(onNavigate: (BaseIdentityNavigation) -> Unit) {
    composable(CreateIdentity) { navBackStack ->
        val selectVault by navBackStack.savedStateHandle
            .getStateFlow<String?>(KEY_VAULT_SELECTED, null)
            .collectAsStateWithLifecycle()

        CreateIdentityScreen(
            selectVault = selectVault.toOption().map { ShareId(it) }.value(),
            onNavigate = onNavigate
        )
    }
}

