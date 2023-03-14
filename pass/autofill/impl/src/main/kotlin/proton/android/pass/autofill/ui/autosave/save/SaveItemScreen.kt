package proton.android.pass.autofill.ui.autosave.save

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.autofill.entities.SaveInformation
import proton.android.pass.autofill.entities.SaveItemType
import proton.android.pass.featureitemcreate.impl.login.CreateLoginScreen
import proton.android.pass.featureitemcreate.impl.login.InitialCreateLoginUiState

const val SAVE_ITEM_ROUTE = "save/item"

@Composable
fun SaveItemScreen(
    modifier: Modifier = Modifier,
    info: SaveInformation,
    onSaved: () -> Unit
) {
    val (username, password) = when (info.itemType) {
        is SaveItemType.Login -> Pair(info.itemType.identity, info.itemType.password)
        is SaveItemType.SingleValue -> Pair(info.itemType.contents, info.itemType.contents)
    }
    val initialContents = InitialCreateLoginUiState(
        title = info.appName,
        username = username,
        password = password,
        url = null
    )

    CreateLoginScreen(
        modifier = modifier,
        showCreateAliasButton = false,
        initialContents = initialContents,
        onClose = onSaved, // For now we consider close to be the same as success
        onSuccess = { onSaved() },
        onScanTotp = {}
    )
}
