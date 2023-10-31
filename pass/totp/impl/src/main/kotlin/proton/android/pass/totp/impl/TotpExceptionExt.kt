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

package proton.android.pass.totp.impl

import proton.android.pass.commonrust.TotpException
import proton.android.pass.totp.api.TotpUriException

@Suppress("ComplexMethod")
fun TotpException.toTotpUriException(): TotpUriException = when (this) {
    is TotpException.EmptySecret ->
        TotpUriException.EmptySecret(message ?: "")
    is TotpException.InvalidAlgorithm ->
        TotpUriException.InvalidAlgorithm(message ?: "")
    is TotpException.InvalidAuthority ->
        TotpUriException.InvalidAuthority(message ?: "")
    is TotpException.InvalidScheme ->
        TotpUriException.InvalidScheme(message ?: "")
    is TotpException.NoAuthority ->
        TotpUriException.NoAuthority(message ?: "")
    is TotpException.NoQueries ->
        TotpUriException.NoQueries(message ?: "")
    is TotpException.NoSecret ->
        TotpUriException.NoSecret(message ?: "")
    is TotpException.NotTotpUri ->
        TotpUriException.NotTotpUri(message ?: "")
    is TotpException.SecretParseException ->
        TotpUriException.SecretParseException(message ?: "")
    is TotpException.SystemTimeException ->
        TotpUriException.SystemTimeException(message ?: "")
    is TotpException.UrlParseException ->
        TotpUriException.UrlParseException(message ?: "")
}
