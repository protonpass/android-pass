package me.proton.pass.autofill.sample.simplecompose

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.headline
import me.proton.core.compose.theme.subheadline
import me.proton.pass.autofill.sample.LoginResultActivity

class SimpleComposeLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ExplicitAutofillTypesDemo(onLoginClicked = {
                val intent = Intent(this, LoginResultActivity::class.java)
                startActivity(intent)
                finish()
            })
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
fun ExplicitAutofillTypesDemo(onLoginClicked: () -> Unit) {
    Column(modifier = Modifier.padding(20.dp)) {
        var nameState by remember { mutableStateOf("") }
        var emailState by remember { mutableStateOf("") }
        val autofill = LocalAutofill.current
        val labelStyle = ProtonTheme.typography.subheadline
        val textStyle = ProtonTheme.typography.headline

        Text("Email", style = labelStyle)
        Autofill(
            autofillTypes = listOf(AutofillType.EmailAddress),
            onFill = { nameState = it }
        ) { autofillNode ->
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth()
                    .onFocusChanged {
                        autofill?.apply {
                            if (it.isFocused) {
                                requestAutofillForNode(autofillNode)
                            } else {
                                cancelAutofillForNode(autofillNode)
                            }
                        }
                    },
                value = nameState,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default
                ),
                onValueChange = { nameState = it },
                textStyle = textStyle
            )
        }

        Spacer(Modifier.height(40.dp))

        Text("Password", style = labelStyle)
        Autofill(
            autofillTypes = listOf(AutofillType.Password),
            onFill = { emailState = it }
        ) { autofillNode ->
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth()
                    .onFocusChanged {
                        autofill?.run {
                            if (it.isFocused) {
                                requestAutofillForNode(autofillNode)
                            } else {
                                cancelAutofillForNode(autofillNode)
                            }
                        }
                    },
                value = emailState,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default
                ),
                onValueChange = { emailState = it },
                textStyle = textStyle
            )
        }

        Spacer(Modifier.height(40.dp))

        Button(onClick = onLoginClicked) {
            Text("Login")
        }
    }
}

@ExperimentalComposeUiApi
@Composable
private fun Autofill(
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit),
    content: @Composable (AutofillNode) -> Unit
) {
    val autofillNode = AutofillNode(onFill = onFill, autofillTypes = autofillTypes)

    val autofillTree = LocalAutofillTree.current
    autofillTree += autofillNode

    Box(
        Modifier.onGloballyPositioned {
            autofillNode.boundingBox = it.boundsInWindow()
        }
    ) {
        content(autofillNode)
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_ExplicitAutofillTypeDemo() {
    ExplicitAutofillTypesDemo(onLoginClicked = {})
}
