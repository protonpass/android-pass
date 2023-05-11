package proton.android.pass.featurepassword.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featurepassword.impl.bottomsheet.GeneratePasswordBottomSheet
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.bottomSheet

object GeneratePasswordBottomsheetMode : NavArgId {
    override val key: String = "mode"
    override val navType = NavType.StringType
}

enum class GeneratePasswordBottomsheetModeValue {
    CopyAndClose,
    CancelConfirm
}

object GeneratePasswordBottomsheet : NavItem(
    baseRoute = "password/create/bottomsheet",
    navArgIds = listOf(GeneratePasswordBottomsheetMode)
) {
    fun buildRoute(mode: GeneratePasswordBottomsheetModeValue) =
        "$baseRoute/${mode.name}"
}

fun NavGraphBuilder.generatePasswordBottomsheetGraph(
    onDismiss: () -> Unit
) {
    bottomSheet(GeneratePasswordBottomsheet) {
        GeneratePasswordBottomSheet(onDismiss = onDismiss)
    }
}
