package me.proton.android.pass.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.android.showkase.models.Showkase

@Composable
fun InternalDrawerContents(
    modifier: Modifier = Modifier
) {
    val viewModel: InternalDrawerViewModel = hiltViewModel()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Spacer(modifier = Modifier.height(10.dp))
        val localContext = LocalContext.current
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                localContext.startActivity(Showkase.getBrowserIntent(localContext))
            }
        ) {
            Text(text = "Showkase")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.clearPreferences() },
        ) {
            Text(text = "Clear preferences")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.shareLogCatOutput(localContext) },
        ) {
            Text(text = "Share Logs")
        }
    }
}
