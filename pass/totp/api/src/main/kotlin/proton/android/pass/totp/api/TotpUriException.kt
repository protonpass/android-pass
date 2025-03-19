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

package proton.android.pass.totp.api

sealed class TotpUriException(message: String) : IllegalArgumentException(message) {
    class NotTotpUri(message: String) : TotpUriException(message)
    class InvalidAuthority(message: String) : TotpUriException(message)
    class NoAuthority(message: String) : TotpUriException(message)
    class InvalidAlgorithm(message: String) : TotpUriException(message)
    class InvalidScheme(message: String) : TotpUriException(message)
    class UrlParseException(message: String) : TotpUriException(message)
    class NoSecret(message: String) : TotpUriException(message)
    class EmptySecret(message: String) : TotpUriException(message)
    class NoQueries(message: String) : TotpUriException(message)
    class SecretParseException(message: String) : TotpUriException(message)
    class InvalidDigitsException(message: String) : TotpUriException(message)
    class InvalidPeriodException(message: String) : TotpUriException(message)
}
