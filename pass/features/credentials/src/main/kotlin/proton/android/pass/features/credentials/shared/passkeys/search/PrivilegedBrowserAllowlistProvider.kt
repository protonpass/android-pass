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

package proton.android.pass.features.credentials.shared.passkeys.search

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.features.credentials.R
import javax.inject.Inject

internal interface PrivilegedBrowserAllowlistProvider {
    val json: String
}

internal class ResourcePrivilegedBrowserAllowlistProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PrivilegedBrowserAllowlistProvider {

    override val json: String by lazy {
        context.resources
            .openRawResource(R.raw.passkey_privileged_browsers_allowlist)
            .bufferedReader()
            .use { reader -> reader.readText() }
    }

    private companion object {
        // Vendored snapshot of Google's passkey privileged-apps allowlist.
        // Upstream: https://www.gstatic.com/gpm-passkeys-privileged-apps/apps.json
        // Snapshot date: 2026-04-20.
        // Refresh through reviewed source changes; do not fetch at runtime.
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PrivilegedBrowserAllowlistProviderModule {

    @Binds
    abstract fun bindPrivilegedBrowserAllowlistProvider(
        impl: ResourcePrivilegedBrowserAllowlistProvider
    ): PrivilegedBrowserAllowlistProvider
}
