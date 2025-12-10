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

package proton.android.pass.autofill.sample.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import proton.android.pass.autofill.sample.databinding.ActivityWebviewLoginBinding
import proton.android.pass.autofill.sample.utils.enableEdgeToEdgeProtonPassCompat

class WebViewLoginActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityWebviewLoginBinding.inflate(layoutInflater)
        enableEdgeToEdgeProtonPassCompat(view = binding.root)

        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                loadWithOverviewMode = false
            }
            webViewClient = WebViewClient()

            if (url == null) loadUrl(URL)
        }

        setContentView(binding.root)
    }

    companion object {
        @Suppress("MaxLineLength")
        private const val URL = "https://www.autofilth.lol/form?definition=JTdCJTIyaWQlMjIlM0ElMjJsb2dpbiUyMiUyQyUyMm5hbWUlMjIlM0ElMjIlRjAlOUYlOTElQTQlMjBMb2dpbiUyMHVzZXJuYW1lJTIyJTJDJTIyZGVzY3JpcHRpb24lMjIlM0ElMjJMb2dpbiUyMGZvcm0lMjB3aXRoJTIwcmVxdWlyZWQlMjB1c2VybmFtZSUyMGFuZCUyMHBhc3N3b3JkJTIwZmllbGRzLiUyMiUyQyUyMmZpZWxkcyUyMiUzQSU1QiU3QiUyMmlkJTIyJTNBJTIydXNlcm5hbWUlMjIlMkMlMjJsYWJlbCUyMiUzQSUyMlVzZXJuYW1lJTIyJTJDJTIyYXV0b2NvbXBsZXRlJTIyJTNBJTIydXNlcm5hbWUlMjIlMkMlMjJyZXF1aXJlZCUyMiUzQXRydWUlN0QlMkMlN0IlMjJpZCUyMiUzQSUyMnBhc3N3b3JkJTIyJTJDJTIydHlwZSUyMiUzQSUyMnBhc3N3b3JkJTIyJTJDJTIybGFiZWwlMjIlM0ElMjJQYXNzd29yZCUyMiUyQyUyMmF1dG9jb21wbGV0ZSUyMiUzQSUyMmN1cnJlbnQtcGFzc3dvcmQlMjIlMkMlMjJyZXF1aXJlZCUyMiUzQXRydWUlN0QlNUQlMkMlMjJzdWJtaXRUZXh0JTIyJTNBJTIyTG9naW4lMjIlMkMlMjJlcnJvclRleHQlMjIlM0ElMjIlRTIlOUQlOEMlMjBTb21ldGhpbmclMjB3ZW50JTIwd3JvbmclMjIlMkMlMjJva1RleHQlMjIlM0ElMjIlRTIlOUMlODUlMjBMb2dnZWQlMjBpbiUyMCElMjIlN0Q%3D"
    }

}
