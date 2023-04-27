package proton.android.pass.featureaccount.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.buttons.UpgradeButton
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar

@Composable
fun AccountContent(
    modifier: Modifier = Modifier,
    state: AccountUiState,
    onSubscriptionClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    onUpClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(R.string.account_title),
                actions = {
                    if (state.showUpgradeButton) {
                        UpgradeButton(
                            modifier = Modifier.padding(12.dp, 0.dp),
                            onUpgradeClick = onUpgradeClick
                        )
                    }
                },
                onUpClick = onUpClick
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(PassTheme.colors.backgroundStrong)
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccountInfo(state = state)
            ManageSubscription(onSubscriptionClick = onSubscriptionClick)
            SignOut(onSignOutClick = onSignOutClick)
            DeleteAccount(onDeleteAccountClick = onDeleteAccountClick)
            Text(
                text = stringResource(R.string.account_permanently_delete_warning),
                style = ProtonTheme.typography.captionWeak
            )
        }
    }
}

