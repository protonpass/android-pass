/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.report.presentation

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import proton.android.pass.commonrust.api.EmailValidator

@Parcelize
data class ReportFormData(
    val description: String = "",
    val email: String = "",
    val username: String = "",
    val attachLog: Boolean = true,
    val extraFiles: Set<Uri> = emptySet()
) : Parcelable {

    companion object {
        private const val DESCRIPTION_MIN_LENGTH: Int = 10
        private const val DESCRIPTION_MAX_LENGTH: Int = 1000

        fun ReportFormData.validate(emailValidator: EmailValidator): List<ReportValidationError> = buildList {
            when {
                email.isBlank() -> add(EmailBlank)
                emailValidator.isValid(email).not() -> add(EmailInvalid)
                description.isBlank() -> add(DescriptionBlank)
                description.length < DESCRIPTION_MIN_LENGTH -> add(DescriptionTooShort)
                description.length > DESCRIPTION_MAX_LENGTH -> add(DescriptionTooLong)
            }
        }
    }
}
