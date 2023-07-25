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

package proton.android.pass.common.api

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.common.api.CommonRegex.EMAIL_VALIDATION_REGEX

class CommonRegexTest {

    @Test
    fun validEmailsShouldMatch() {
        val validEmails = arrayOf(
            "user@example.com",
            "user123@example.co.uk",
            "user.name123@example.com",
            "user+label@example.co.uk",
            "user@subdomain.example.com",
            "user@192.168.0.1",
            "user@[192.168.0.1]"
        )

        for (email in validEmails) {
            assertThat(email).matches(EMAIL_VALIDATION_REGEX.pattern)
        }
    }

    @Test
    fun invalidEmailsShouldNotMatch() {
        val invalidEmails = arrayOf(
            "user@",
            "@example.com",
            "user@.com",
            "user@invalid domain.com",
            "user@_invalid.com",
            "user@ex!ample.com",
            "user@exam ple.com",
            "user@-example.com",
            "user@[192.168.0.300]"
        )

        for (email in invalidEmails) {
            assertThat(email).doesNotMatch(EMAIL_VALIDATION_REGEX.pattern)
        }
    }
}
