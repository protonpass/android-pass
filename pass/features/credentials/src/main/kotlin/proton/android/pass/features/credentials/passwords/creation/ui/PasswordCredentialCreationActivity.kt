/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.credentials.passwords.creation.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import proton.android.pass.features.credentials.passwords.creation.presentation.PasswordCredentialCreationViewModel

@[AndroidEntryPoint RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)]
internal class PasswordCredentialCreationActivity : FragmentActivity() {

    private val viewModel: PasswordCredentialCreationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val request = getPasswordCredentialCreationRequest()
//        println("JIBIRI: Create password credential request: $request")
//        viewModel.onUpdateRequest(request)

        viewModel.onRegister(this)


    }

//    private fun getPasswordCredentialCreationRequest(): PasswordCredentialCreationRequest? = null

}
