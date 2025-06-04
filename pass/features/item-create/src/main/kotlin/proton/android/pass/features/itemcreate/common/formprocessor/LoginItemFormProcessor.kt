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

import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.features.itemcreate.common.CommonFieldValidationError
import proton.android.pass.features.itemcreate.common.LoginItemValidationError
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.features.itemcreate.login.LoginItemFormState
import javax.inject.Inject
import javax.inject.Singleton

private typealias PrimaryTotpFormProcessorType =
    FormProcessor<PrimaryTotpFormProcessor.Input, UIHiddenState>

@Singleton
class LoginItemFormProcessor @Inject constructor(
    private val primaryTotpFormProcessor: PrimaryTotpFormProcessorType,
    private val customFieldFormProcessor: CustomFieldFormProcessorType
) : FormProcessor<LoginItemFormProcessor.Input, LoginItemFormState> {

    data class Input(
        val originalPrimaryTotp: Option<UIHiddenState>,
        val originalCustomFields: List<UICustomFieldContent>,
        val formState: LoginItemFormState
    )

    override suspend fun process(
        input: Input,
        decrypt: (EncryptedString) -> String,
        encrypt: (String) -> EncryptedString
    ): FormProcessingResult<LoginItemFormState> {
        val errors = mutableSetOf<ValidationError>()

        if (input.formState.title.isBlank()) {
            errors += CommonFieldValidationError.BlankTitle
        }
        val sanitisedUrls = input.formState.urls.mapIndexed { idx, url ->
            if (url.isNotBlank()) {
                UrlSanitizer.sanitize(url)
                    .fold(
                        onFailure = {
                            errors += LoginItemValidationError.InvalidUrl(idx)
                            url
                        },
                        onSuccess = {
                            it
                        }
                    )
            } else {
                ""
            }
        }
        val primaryTotpResult = primaryTotpFormProcessor.process(
            input = PrimaryTotpFormProcessor.Input(
                originalPrimaryTotp = input.originalPrimaryTotp,
                primaryTotp = input.formState.primaryTotp
            ),
            decrypt = decrypt,
            encrypt = encrypt
        )
        if (primaryTotpResult is FormProcessingResult.Error) {
            errors.addAll(primaryTotpResult.errors)
        }
        val customFieldResult = customFieldFormProcessor.process(
            input = UICustomFieldContentFormProcessor.Input(
                customFields = input.formState.customFields,
                originalCustomFields = input.originalCustomFields
            ),
            decrypt = decrypt,
            encrypt = encrypt
        )
        if (customFieldResult is FormProcessingResult.Error) {
            errors.addAll(customFieldResult.errors)
        }
        return if (
            errors.isEmpty() &&
            primaryTotpResult is FormProcessingResult.Success &&
            customFieldResult is FormProcessingResult.Success
        ) {
            FormProcessingResult.Success(
                input.formState.copy(
                    primaryTotp = primaryTotpResult.sanitized,
                    urls = sanitisedUrls,
                    customFields = customFieldResult.sanitized
                )
            )
        } else {
            FormProcessingResult.Error(errors)
        }
    }
}
