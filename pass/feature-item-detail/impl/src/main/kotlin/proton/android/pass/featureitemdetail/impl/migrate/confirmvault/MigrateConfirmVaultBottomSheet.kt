package proton.android.pass.featureitemdetail.impl.migrate.confirmvault

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.bottomSheet
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun MigrateConfirmVaultBottomSheet(
    modifier: Modifier = Modifier,
    onMigrated: (ShareId, ItemId) -> Unit,
    onCancel: () -> Unit,
    viewModel: MigrateConfirmVaultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler { onCancel() }

    LaunchedEffect(state.event) {
        val event = state.event
        if (event is Some) {
            when (val value = event.value) {
                is ConfirmMigrateEvent.Migrated -> {
                    onMigrated(value.shareId, value.itemId)
                }
                ConfirmMigrateEvent.Close -> onCancel()
            }
        }
    }

    MigrateConfirmVaultContents(
        modifier = modifier
            .bottomSheet(horizontalPadding = PassTheme.dimens.bottomsheetHorizontalPadding),
        state = state,
        onCancel = { viewModel.onCancel() },
        onConfirm = { viewModel.onConfirm() }
    )
}
