/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.autofill.sample.simplecompose

import android.content.Intent
import android.os.Bundle
import android.view.autofill.AutofillManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import me.proton.core.compose.autofill.autofill
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.autofill.sample.LoginResultActivity
import proton.android.pass.commonui.api.enableEdgeToEdgeProtonPass

class SimpleComposeLoginActivity : AppCompatActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeProtonPass()
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
@OptIn(ExperimentalComposeUiApi::class)
fun ExplicitAutofillTypesDemo(onLoginClicked: () -> Unit) {

    val context = LocalContext.current
    val autofillManager = context.getSystemService(AutofillManager::class.java)


    Column(
        modifier = Modifier
            .padding(20.dp)
            .statusBarsPadding()
    ) {
        var emailState by remember { mutableStateOf("") }
        var passwordState by remember { mutableStateOf("") }

        val labelStyle = ProtonTheme.typography.subheadline
        val textStyle = ProtonTheme.typography.headline

        Text("Email", style = labelStyle)
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .autofill(
                    autofillType = AutofillType.EmailAddress,
                    onFill = {
                        emailState = it
                    }
                ),
            value = emailState,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            onValueChange = { emailState = it },
            textStyle = textStyle
        )

        Spacer(Modifier.height(40.dp))

        Text("Password", style = labelStyle)
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .autofill(
                    autofillType = AutofillType.Password,
                    onFill = {
                        passwordState = it
                    }
                ),
            value = passwordState,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            onValueChange = { passwordState = it },
            textStyle = textStyle,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(40.dp))

        Button(onClick = {
            if (autofillManager?.isAutofillSupported == true) {
                autofillManager.commit()
            }
            onLoginClicked()
        }) {
            Text("Login")
        }
    }
}

