package proton.android.pass.featurevault.impl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.R
import proton.android.pass.composecomponents.impl.topbar.TopBarTitleView
import proton.pass.domain.ShareId

@Composable
fun VaultContent(
    modifier: Modifier = Modifier,
    uiState: VaultListUIState,
    onVaultCreate: () -> Unit,
    onVaultSelect: (ShareId) -> Unit,
    onVaultEdit: (ShareId) -> Unit,
    onVaultDelete: (ShareId) -> Unit
) {
    Scaffold(
        topBar = {
            ProtonTopAppBar(
                modifier = modifier,
                title = { TopBarTitleView(title = "Vault list") },
                actions = {
                    IconButton(
                        onClick = { onVaultCreate() },
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_proton_plus),
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
