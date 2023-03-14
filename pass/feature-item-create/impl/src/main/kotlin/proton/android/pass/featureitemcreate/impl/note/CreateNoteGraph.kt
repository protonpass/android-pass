package proton.android.pass.featureitemcreate.impl.note

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath
import proton.pass.domain.ShareId

object CreateNote : NavItem(
    baseRoute = "note/create",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId)
) {
    fun createNavRoute(shareId: Option<ShareId>) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        val path = map.toPath()
        append(path)
    }
}

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
fun NavGraphBuilder.createNoteGraph(
    onNoteCreateSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    composable(CreateNote) {
        CreateNoteScreen(
            onUpClick = onBackClick,
            onSuccess = onNoteCreateSuccess
        )
    }
}
