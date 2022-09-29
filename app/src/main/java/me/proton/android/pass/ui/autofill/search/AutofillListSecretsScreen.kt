package me.proton.android.pass.ui.autofill.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Password
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.R
import me.proton.android.pass.ui.autofill.search.AutofillListSecretsScreen.SecretItem
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.caption
import me.proton.core.pass.domain.entity.commonsecret.Secret
import me.proton.core.pass.domain.entity.commonsecret.SecretType
import me.proton.core.pass.domain.entity.commonsecret.SecretValue

object AutofillListSecretsScreen {
    const val route = "autofill/secrets"

    @Composable
    fun view(
        viewModel: AutofillSearchSecretViewModel = hiltViewModel(),
        packageName: String,
        onSelectedCredentials: (Secret) -> Unit
    ) {
        val viewModel = remember { viewModel }
        val state by viewModel.state.collectAsState()

        LaunchedEffect(packageName) { viewModel.searchByPackageName(packageName) }

        (state as? AutofillSearchSecretViewModel.State.Ready)?.let {
            ListSecretsDialogContents(it.results, onSelectedCredentials)
        }
    }

    @Composable
    fun ListSecretsDialogContents(
        results: List<ListSecretItem>,
        onSelectedCredentials: (Secret) -> Unit
    ) {
        if (results.isEmpty()) {
            Text(stringResource(R.string.autofill_list_secrets_empty_message))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(top = 20.dp)
            ) {
                items(results) { item ->
                    SecretItem(
                        item = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectedCredentials(item.secret) }
                    )
                }
            }
        }
    }

    @Composable
    fun SecretItem(item: ListSecretItem, modifier: Modifier = Modifier) {
        Row(modifier = modifier.padding(16.dp)) {
            Icon(Icons.Default.Password, "key")
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(item.secret.name)
                Text(item.address, style = ProtonTheme.typography.caption)
            }
        }
    }
}

@Preview
@Composable
private fun Preview_SecretItem() {
    val secret = Secret(
        id = null,
        userId = "user_id",
        addressId = "address_id",
        name = "Some secret",
        type = SecretType.Email,
        isUploaded = false,
        contents = SecretValue.Single("Contents"),
        associatedUris = emptyList()
    )
    val item = ListSecretItem(secret, "address@proton.me")
    SecretItem(item)
}
