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

package proton.android.pass.features.itemcreate.common.formprocessor

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.features.itemcreate.common.LoginItemValidationError
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrimaryTotpFormProcessor @Inject constructor(
    private val totpManager: TotpManager
) : FormProcessor<PrimaryTotpFormProcessor.Input, UIHiddenState> {

    data class Input(
        val originalPrimaryTotp: Option<UIHiddenState>,
        val primaryTotp: UIHiddenState
    )

    override suspend fun process(
        input: Input,
        decrypt: (EncryptedString) -> String,
        encrypt: (String) -> EncryptedString
    ): FormProcessingResult<UIHiddenState> {
        val decrypted = decrypt(input.primaryTotp.encrypted)
        return if (decrypted.isNotBlank()) {
            val originalDecrypted = input.originalPrimaryTotp.value()
                ?.let { decrypt(it.encrypted) }
                .orEmpty()

            totpManager.sanitiseToSave(originalDecrypted, decrypted)
                .fold(
                    onSuccess = { uri ->
                        val parseSuccess = totpManager.parse(uri).isSuccess
                        val codeSuccess = safeRunCatching {
                            totpManager.observeCode(uri).firstOrNull()
                        }.isSuccess

                        if (!parseSuccess || !codeSuccess) {
                            FormProcessingResult.Error(setOf(LoginItemValidationError.InvalidPrimaryTotp))
                        } else {
                            FormProcessingResult.Success(UIHiddenState.Revealed(encrypt(uri), uri))
                        }
                    },
                    onFailure = {
                        FormProcessingResult.Error(setOf(LoginItemValidationError.InvalidPrimaryTotp))
                    }
                )
        } else {
            FormProcessingResult.Success(input.primaryTotp)
        }
    }
}
