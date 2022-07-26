package me.proton.android.pass.ui.autofill.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.android.pass.R

object AutofillAuthenticationScreen {
    const val route = "autofill/authentication"

    @Composable
    fun view(onAuthenticated: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp)
        ) {
            Button(
                onClick = { onAuthenticated() },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(stringResource(R.string.action_authenticate))
            }
        }
    }
}
