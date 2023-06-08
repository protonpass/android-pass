package proton.android.pass.featureitemcreate.impl.bottomsheets.createitem

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
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId),
    isBottomsheet = true
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

sealed interface CreateItemBottomsheetNavigation {
    data class CreateLogin(val shareId: Option<ShareId>) : CreateItemBottomsheetNavigation
    data class CreateAlias(val shareId: Option<ShareId>) : CreateItemBottomsheetNavigation
    data class CreateNote(val shareId: Option<ShareId>) : CreateItemBottomsheetNavigation
    object CreatePassword : CreateItemBottomsheetNavigation
}

fun NavGraphBuilder.bottomsheetCreateItemGraph(
    mode: CreateItemBottomSheetMode,
    onNavigate: (CreateItemBottomsheetNavigation) -> Unit,
) {
    bottomSheet(CreateItemBottomsheet) {
        CreateItemBottomSheet(
            mode = mode,
            onNavigate = onNavigate
        )
    }
}
