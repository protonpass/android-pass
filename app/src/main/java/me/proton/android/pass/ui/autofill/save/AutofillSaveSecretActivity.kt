package me.proton.android.pass.ui.autofill.save

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.pass.R
import me.proton.android.pass.ui.autofill.save.AutofillSaveSecretViewModel.State
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.autofill.service.Constants
import me.proton.core.pass.autofill.service.entities.SecretSaveInfo
import me.proton.core.presentation.utils.showToast
import me.proton.core.user.domain.entity.UserAddress

@AndroidEntryPoint
class AutofillSaveSecretActivity: ComponentActivity() {

    private val viewModel: AutofillSaveSecretViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val saveInfo = requireNotNull(getSecretSaveInfo(intent))

        setContent {
            val state by viewModel.state.collectAsState()

            when (state) {
                is State.Success -> LaunchedEffect(Unit) { finish() }
                is State.Failure -> LaunchedEffect(Unit) {
                        showToast(getString(R.string.error_autofill_save_credentials))
                        finish()
                }
                else -> {
                    ProtonTheme {
                        SaveCredentialsDialogContents(state, saveInfo) { address, saveInfo ->
                            saveSecret(address, saveInfo)
                        }
                    }
                }
            }
        }
    }

    private fun getSecretSaveInfo(intent: Intent): SecretSaveInfo? =
        intent.getParcelableExtra(Constants.ARG_SAVE_CREDENTIALS_SECRET)

    private fun saveSecret(address: UserAddress, secretSaveInfo: SecretSaveInfo) {
        viewModel.save(address, secretSaveInfo)
    }

}
