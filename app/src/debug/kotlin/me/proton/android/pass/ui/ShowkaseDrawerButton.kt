package me.proton.android.pass.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.airbnb.android.showkase.models.Showkase

@Composable
fun ShowkaseDrawerButton(modifier: Modifier = Modifier) {
    val localContext = LocalContext.current
    Button(
        modifier = modifier.fillMaxWidth(),
        onClick = {
            localContext.startActivity(Showkase.getBrowserIntent(localContext))
        }
    ) {
        Text(text = "Showkase")
    }
}
