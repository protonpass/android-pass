package proton.android.pass.featurepassword.impl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.featurepassword.impl.bottomsheet.GeneratePasswordBottomSheet
import proton.android.pass.featurepassword.impl.dialog.mode.passwordModeDialog
import proton.android.pass.featurepassword.impl.dialog.separator.wordSeparatorDialog
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

sealed interface GeneratePasswordNavigation {
    object DismissBottomsheet : GeneratePasswordNavigation
    object CloseDialog : GeneratePasswordNavigation
    object OnSelectWordSeparator : GeneratePasswordNavigation
    object OnSelectPasswordMode : GeneratePasswordNavigation
}

fun NavGraphBuilder.generatePasswordBottomsheetGraph(
    onNavigate: (GeneratePasswordNavigation) -> Unit
) {
    bottomSheet(GeneratePasswordBottomsheet) {
        GeneratePasswordBottomSheet(onNavigate = onNavigate)
    }

    wordSeparatorDialog(onNavigate = onNavigate)
    passwordModeDialog(onNavigate = onNavigate)
}
