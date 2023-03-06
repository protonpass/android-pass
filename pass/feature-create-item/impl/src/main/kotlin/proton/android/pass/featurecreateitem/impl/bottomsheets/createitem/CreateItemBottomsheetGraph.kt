package proton.android.pass.featurecreateitem.impl.bottomsheets.createitem

import androidx.compose.material.ExperimentalMaterialApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.toPath
import proton.pass.domain.ShareId

object CreateItemBottomsheet : NavItem(
    baseRoute = "item/create/bottomsheet",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId)
) {
    fun createNavRoute(
        shareId: Option<ShareId> = None,
    ) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        val path = map.toPath()
        append(path)
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun NavGraphBuilder.bottomsheetCreateItemGraph(
    onCreateLogin: (Option<ShareId>) -> Unit,
    onCreateAlias: (Option<ShareId>) -> Unit,
    onCreateNote: (Option<ShareId>) -> Unit,
    onCreatePassword: () -> Unit
) {
    bottomSheet(CreateItemBottomsheet) {
        CreateItemBottomSheetContents(
            onCreateLogin = onCreateLogin,
            onCreateAlias = onCreateAlias,
            onCreateNote = onCreateNote,
            onCreatePassword = onCreatePassword
        )
    }
}
