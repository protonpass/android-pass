package proton.android.pass.featureitemcreate.impl.login

import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.customFieldBottomSheetGraph
import proton.android.pass.featureitemcreate.impl.dialogs.CustomFieldNameNavigation
import proton.android.pass.featureitemcreate.impl.dialogs.customFieldNameDialogGraph
import proton.android.pass.featureitemcreate.impl.login.bottomsheet.aliasoptions.aliasOptionsBottomSheetGraph

fun NavGraphBuilder.createUpdateLoginGraph(
    initialCreateLoginUiState: InitialCreateLoginUiState = InitialCreateLoginUiState(),
    showCreateAliasButton: Boolean = true,
    onNavigate: (BaseLoginNavigation) -> Unit
) {
    createLoginGraph(initialCreateLoginUiState, showCreateAliasButton, onNavigate)
    updateLoginGraph(onNavigate)

    aliasOptionsBottomSheetGraph(onNavigate)
    customFieldBottomSheetGraph(onNavigate)
    customFieldNameDialogGraph {
        when (it) {
            is CustomFieldNameNavigation.Close -> {
                onNavigate(BaseLoginNavigation.Close)
            }
        }
    }
}
