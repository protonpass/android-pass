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

package proton.android.pass.browserallowlist.api

/**
 * The single vendored copy of Google's passkey privileged-apps allowlist
 * (https://www.gstatic.com/gpm-passkeys-privileged-apps/apps.json), shared by every
 * feature that needs to recognize a known browser by its signing certificate rather
 * than by package name alone. Consumers parse [json] into whatever shape they need.
 */
interface PrivilegedBrowserAllowlistProvider {
    val json: String
}
