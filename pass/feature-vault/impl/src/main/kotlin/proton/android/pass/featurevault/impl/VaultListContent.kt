package proton.android.pass.featurevault.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.commonui.api.PassDimens.bottomSheetPadding
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.topbar.TopBarTitleView
import proton.android.pass.composecomponents.impl.topbar.icon.CrossBackIcon
import proton.android.pass.feature.vault.impl.R
import proton.pass.domain.ShareId

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
fun VaultListContent(
    modifier: Modifier = Modifier,
    uiState: VaultListUIState,
    onVaultCreate: () -> Unit,
    onVaultSelect: (ShareId) -> Unit,
    onVaultEdit: (ShareId) -> Unit,
    onVaultMigrate: (ShareId?, ShareId) -> Unit,
    onVaultDelete: (ShareId) -> Unit,
    onUpClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var vaultToMigrate: Option<ShareUiModel> by remember { mutableStateOf(None) }
    var vaultToDelete: Option<ShareUiModel> by remember { mutableStateOf(None) }
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    PassModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            Column(modifier = Modifier.bottomSheetPadding()) {
                BottomSheetTitle(title = stringResource(R.string.vault_delete_bottomsheet_title), showDivider = false)
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .bottomSheetPadding()
                ) {
                    items(items = uiState.list.filter { it.id != vaultToMigrate.value()?.id }) { item ->
                        MigrateVaultItem(
                            share = item,
                            onVaultSelect = {
                                onVaultMigrate(vaultToMigrate.value()?.id, item.id)
                                scope.launch {
                                    bottomSheetState.hide()
                                }
                            }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                ProtonTopAppBar(
                    modifier = modifier,
                    title = { TopBarTitleView(title = stringResource(id = R.string.vault_list_top_bar_title)) },
                    navigationIcon = { CrossBackIcon(onUpClick = onUpClick) },
                    actions = {
                        IconButton(
                            onClick = { onVaultCreate() },
                            modifier = Modifier.padding(end = 10.dp)
                        ) {
                            Icon(
                                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_plus),
                                contentDescription = null,
                                tint = ProtonTheme.colors.iconNorm
                            )
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(items = uiState.list) { item ->
                    VaultItem(
                        share = item,
                        isSelected = item.id.id == uiState.currentShare?.id,
                        onVaultSelect = { onVaultSelect(it.id) },
                        onVaultEdit = { onVaultEdit(it.id) },
                        onVaultDelete = {
                            vaultToDelete = it.toOption()
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }
    VaultDeleteDialog(
        show = showDeleteDialog,
        shareUiModel = vaultToDelete.value(),
        onMigrate = {
            showDeleteDialog = false
            vaultToMigrate = it.toOption()
            scope.launch {
                bottomSheetState.show()
            }
        },
        onDelete = {
            onVaultDelete(it.id)
            showDeleteDialog = false
        },
        onCancel = {
            vaultToDelete = None
            showDeleteDialog = false
        },
        onDismiss = { showDeleteDialog = false }
    )
}
