package proton.android.pass.featurevault.impl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.composecomponents.impl.topbar.TopBarTitleView
import proton.android.pass.composecomponents.impl.topbar.icon.CrossBackIcon
import proton.android.pass.feature.vault.impl.R
import proton.pass.domain.ShareId

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VaultListContent(
    modifier: Modifier = Modifier,
    uiState: VaultListUIState,
    onVaultCreate: () -> Unit,
    onVaultSelect: (ShareId) -> Unit,
    onVaultEdit: (ShareId) -> Unit,
    onVaultDelete: (ShareId) -> Unit,
    onUpClick: () -> Unit
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
                    onVaultSelect = onVaultSelect,
                    onVaultEdit = onVaultEdit,
                    onVaultDelete = onVaultDelete
                )
            }
        }
    }
}
