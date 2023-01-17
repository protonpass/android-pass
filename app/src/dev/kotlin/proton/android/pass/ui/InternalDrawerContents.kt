package proton.android.pass.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.log.api.PassLogger

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun InternalDrawerContents(
    modifier: Modifier = Modifier,
    viewModel: InternalDrawerViewModel = hiltViewModel()
) {
    val state by viewModel.shareUIState.collectAsStateWithLifecycle()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(modifier = Modifier.height(10.dp))
        ShowkaseDrawerButton()
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.clearPreferences() },
        ) {
            Text(text = "Clear preferences")
        }
        Spacer(modifier = Modifier.height(10.dp))
        val localContext = LocalContext.current
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.shareLogCatOutput(localContext) },
        ) {
            Text(text = "Share Logs")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                try {
                    throw DeveloperException("This is a test.")
                } catch (e: DeveloperException) {
                    PassLogger.e("Internal", e)
                }
            },
        ) {
            Text(text = "Trigger Crash")
        }
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Current share ${state.currentShare.value()?.id?.take(ID_LENGTH) ?: "None"}")

        state.list.forEach {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.changeCurrentShareToPerformActions(it.id) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = it.id.id.take(ID_LENGTH) == state.shareToModify.value()?.id?.take(ID_LENGTH),
                    onClick = {
                        viewModel.changeCurrentShareToPerformActions(it.id)
                    }
                )
                Text(text = it.id.id.take(ID_LENGTH))
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.deleteVault() },
        ) {
            Text(text = "Delete vault")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.onCreateVault() },
        ) {
            Text(text = "Create vault")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.changeSelectedVault() },
        ) {
            Text(text = "Change selected vault")
        }
    }
}
private const val ID_LENGTH = 5
class DeveloperException(message: String) : Exception(message)
