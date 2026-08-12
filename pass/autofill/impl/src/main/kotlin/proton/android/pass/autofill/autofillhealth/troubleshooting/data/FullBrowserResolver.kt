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

package proton.android.pass.autofill.autofillhealth.troubleshooting.data

/**
 * Decides whether an activity resolved for a browsable web intent is a full browser.
 *
 * Many apps declare `ACTION_VIEW` + `CATEGORY_BROWSABLE` filters without being browsers: deep links
 * restrict themselves to specific hosts, and apps embedding a web view usually declare a single
 * scheme. A full browser handles both `http` and `https` for any host.
 */
object FullBrowserResolver {

    private const val SCHEME_HTTP = "http"
    private const val SCHEME_HTTPS = "https"

    fun isFullBrowser(hasAuthorities: Boolean, schemes: Set<String>): Boolean = when {
        hasAuthorities -> false
        else -> SCHEME_HTTP in schemes && SCHEME_HTTPS in schemes
    }
}
