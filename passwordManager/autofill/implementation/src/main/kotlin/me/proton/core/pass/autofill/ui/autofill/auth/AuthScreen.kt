package me.proton.core.pass.autofill.ui.autofill.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

const val AUTH_SCREEN_ROUTE = "autofill/auth"

@Composable
fun AuthScreen(
    onAuthSuccessful: () -> Unit
) {
    Button(
        onClick = onAuthSuccessful,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Auth")
    }
}
