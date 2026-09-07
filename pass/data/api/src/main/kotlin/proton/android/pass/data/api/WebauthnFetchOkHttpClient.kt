/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.api

import javax.inject.Qualifier

/**
 * An OkHttpClient for the webauthn related-origins fetch (`.well-known/webauthn`). The passkey
 * library is designed to follow redirects here and validate the redirect target's domain via
 * the response's final URL, so this client still follows HTTPS -> HTTPS redirects. It only
 * blocks an HTTPS -> HTTP downgrade, which the library's domain-only check cannot catch.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WebauthnFetchOkHttpClient
