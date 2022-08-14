package me.proton.android.pass.ui.create.password

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import me.proton.android.pass.ui.shared.ArrowBackIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreatePasswordView(
    @StringRes topBarTitle: Int,
    onUpClick: () -> Unit,
    onConfirm: (String) -> Unit
) {
    Scaffold(
        topBar = {
            ProtonTopAppBar(
                title = { TopBarTitleView(topBarTitle) },
                navigationIcon = { ArrowBackIcon(onUpClick = onUpClick) },
                actions = {},
            )
        }) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text("Create password")
        }
    }
}