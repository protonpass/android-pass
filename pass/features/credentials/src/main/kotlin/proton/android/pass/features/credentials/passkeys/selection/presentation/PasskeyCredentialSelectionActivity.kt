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

package proton.android.pass.features.credentials.passkeys.selection.presentation

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.credentials.provider.BeginGetPublicKeyCredentialOption
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import proton.android.pass.data.api.usecases.passkeys.PasskeyItem
import proton.android.pass.features.credentials.shared.passkeys.domain.PasskeyRequestType

@[AndroidEntryPoint RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)]
internal class PasskeyCredentialSelectionActivity : FragmentActivity() {

    internal companion object {

        private const val TAG = "PasskeyCredentialSelectionActivity"

        private const val EXTRAS_REQUEST_TYPE_KEY = "REQUEST_TYPE"
        private const val EXTRAS_SHARE_ID = "SHARE_ID"
        private const val EXTRAS_ITEM_ID = "ITEM_ID_ID"
        private const val EXTRAS_PASSKEY_ID = "PASSKEY_ID"
        private const val EXTRAS_REQUEST_JSON = "REQUEST_JSON"
        private const val EXTRAS_REQUEST_ORIGIN = "REQUEST_ORIGIN"
        private const val EXTRAS_REQUEST_CLIENT_DATA_HASH = "REQUEST_CLIENT_DATA_HASH"

        internal fun createPasskeyCredentialIntent(context: Context, passkeyItem: PasskeyItem): Intent = Intent(
            context,
            PasskeyCredentialSelectionActivity::class.java
        ).apply {
            setPackage(context.packageName)

            putExtra(EXTRAS_REQUEST_TYPE_KEY, PasskeyRequestType.UsePasskey.name)
            putExtra(EXTRAS_SHARE_ID, passkeyItem.shareId.id)
            putExtra(EXTRAS_ITEM_ID, passkeyItem.itemId.id)
            putExtra(EXTRAS_PASSKEY_ID, passkeyItem.passkey.id.value)
        }

        internal fun createPasskeyCredentialIntent(
            context: Context,
            origin: String,
            option: BeginGetPublicKeyCredentialOption
        ): Intent = Intent(
            context,
            PasskeyCredentialSelectionActivity::class.java
        ).apply {
            setPackage(context.packageName)

            putExtra(EXTRAS_REQUEST_TYPE_KEY, PasskeyRequestType.SelectPasskey.name)
            putExtra(EXTRAS_REQUEST_ORIGIN, origin)
            putExtra(EXTRAS_REQUEST_JSON, option.requestJson)
            putExtra(EXTRAS_REQUEST_CLIENT_DATA_HASH, option.clientDataHash)
        }

    }
}
