package proton.android.pass.featureaccount.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar

@Composable
fun AccountContent(
    modifier: Modifier = Modifier,
    state: AccountUiState,
    onManageSubscriptionClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onUpClick: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(R.string.account_title),
                onUpClick = onUpClick
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccountInfo(state = state)
            ManageSubscription(onManageSubscriptionClick = onManageSubscriptionClick)
            SignOut(onSignOutClick = onSignOutClick)
            DeleteAccount(onDeleteAccountClick = onDeleteAccountClick)
            Text(
                text = stringResource(R.string.account_permanently_delete_warning),
                style = ProtonTheme.typography.captionWeak
            )
        }
    }
}

