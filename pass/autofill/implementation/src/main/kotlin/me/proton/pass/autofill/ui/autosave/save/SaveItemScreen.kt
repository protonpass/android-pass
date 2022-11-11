package me.proton.pass.autofill.ui.autosave.save

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import me.proton.pass.autofill.entities.SaveInformation
import me.proton.pass.autofill.entities.SaveItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.create.login.CreateLoginWithInitialContents
import me.proton.pass.presentation.create.login.InitialCreateLoginUiState

const val SAVE_ITEM_ROUTE = "save/item"


@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SaveItemScreen(
    modifier: Modifier,
    info: SaveInformation,
    onSaved: () -> Unit,
    onCreateAliasClick: (ShareId) -> Unit
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

    CreateLoginWithInitialContents(
        modifier = modifier,
        initialContents = initialContents,
        onClose = onSaved, // For now we consider close to be the same as success
        onSuccess = onSaved,
        onCreateAliasClick = onCreateAliasClick
    )
}
