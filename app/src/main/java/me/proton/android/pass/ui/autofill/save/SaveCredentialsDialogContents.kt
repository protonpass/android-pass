package me.proton.android.pass.ui.autofill.save

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.android.pass.R
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.autofill.service.entities.SecretSaveInfo
import me.proton.core.pass.domain.entity.commonsecret.SecretType
import me.proton.core.pass.domain.entity.commonsecret.SecretValue
import me.proton.core.pass.presentation.components.common.TextFieldDropdownMenu
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress

@Composable
fun SaveCredentialsDialogContents(
    state: AutofillSaveSecretViewModel.State,
    saveInfo: SecretSaveInfo,
    onSubmit: (UserAddress, SecretSaveInfo) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var appName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(saveInfo.name))
    }
    val scrollState = rememberScrollState()
    val accounts = (state as? AutofillSaveSecretViewModel.State.Ready)?.accounts.orEmpty()

    var selectedAccountIndex by remember { mutableStateOf(0) }
    var selectedAddress by remember {
        mutableStateOf(accounts.getOrNull(selectedAccountIndex)?.addresses?.firstOrNull())
    }
    Column(
        Modifier
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        val accountTextValues = accounts.map { it.account.username }
        val addressesTextValues = accounts.getOrNull(selectedAccountIndex)?.addresses
            ?.map { it.email }
            .orEmpty()

        TextFieldDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.autofill_save_user_label)) },
            values = accountTextValues,
            onSelected = { index ->
                selectedAccountIndex = index
            }
        )

        TextFieldDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.autofill_save_address_label)) },
            values = addressesTextValues,
            onSelected = { index ->
                selectedAddress = accounts.getOrNull(selectedAccountIndex)?.addresses?.get(index)
            }
        )

        OutlinedTextField(
            value = appName,
            onValueChange = { appName = it },
            label = { Text(stringResource(R.string.autofill_save_name_label)) },
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val newSaveInfo = saveInfo.copy(name = appName.text)
                selectedAddress?.let {
                    onSubmit(it, newSaveInfo)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(me.proton.core.usersettings.presentation.R.string.settings_save))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_SaveCredentialsDialog() {
    val saveInfo = SecretSaveInfo(
        "Some app",
        "com.app.some",
        SecretType.Login,
        SecretValue.Login("user", "pass")
    )

    val state = AutofillSaveSecretViewModel.State.Ready(
        accounts = listOf(
            AutofillSaveSecretViewModel.AccountWithAddresses(
                AutofillSaveSecretViewModel.AccountData(UserId("user_id"), "Some account"),
                listOf(
                    makeUserAddress("a@b.com", "User A", 0),
                    makeUserAddress("a@b.com", "User B", 1),
                    makeUserAddress("a@b.com", null, 2)
                )
            )
        )
    )
    SaveCredentialsDialogContents(state, saveInfo) { _, _ -> }
}

private fun makeUserAddress(email: String, displayName: String?, order: Int): UserAddress {
    return UserAddress(
        userId = UserId(""),
        addressId = AddressId(""),
        email = email,
        displayName = displayName,
        canSend = true,
        canReceive = true,
        enabled = true,
        order = order,
        keys = emptyList(),
        signedKeyList = null
    )
}
