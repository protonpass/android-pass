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

package proton.android.pass.data.impl.usecases

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CheckPin
import proton.android.pass.data.impl.util.PinFileConfig
import proton.android.pass.log.api.PassLogger
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckPinImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionContextProvider: EncryptionContextProvider
) : CheckPin {
    override suspend fun invoke(pin: ByteArray): Boolean = withContext(Dispatchers.IO) {
        val file = File(context.dataDir, PinFileConfig.FILE_NAME)
        if (!file.exists() || file.isDirectory) {
            PassLogger.w(TAG, "Pin file does not exist")
            return@withContext false
        }
        val encrypted = EncryptedByteArray(file.readBytes())
        val decryptedData = encryptionContextProvider.withEncryptionContext { decrypt(encrypted) }
        decryptedData.contentEquals(pin)
    }

    companion object {
        private const val TAG = "CheckPinImpl"
    }
}
