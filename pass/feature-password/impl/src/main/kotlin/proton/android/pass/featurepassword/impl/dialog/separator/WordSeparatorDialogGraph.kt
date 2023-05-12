package proton.android.pass.featurepassword.impl.dialog.separator

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featurepassword.impl.GeneratePasswordNavigation
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.dialog

object WordSeparatorDialog : NavItem(baseRoute = "password/create/wordseparator")

fun NavGraphBuilder.wordSeparatorDialog(
    onNavigate: (GeneratePasswordNavigation) -> Unit
) {
    dialog(WordSeparatorDialog) {
        WordSeparatorDialog(
            onNavigate = onNavigate
        )
    }
}
