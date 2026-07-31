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

package proton.android.pass.network.impl

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.network.api.NetworkRestrictionDiagnostics
import proton.android.pass.network.api.NetworkRestrictionSnapshot
import javax.inject.Inject

class NetworkRestrictionDiagnosticsImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : NetworkRestrictionDiagnostics {

    override fun snapshot(): NetworkRestrictionSnapshot {
        val connectivityManager = context.getSystemService<ConnectivityManager>()
        return NetworkRestrictionSnapshot(
            isActiveNetworkMetered = connectivityManager?.isActiveNetworkMetered ?: false,
            isBackgroundDataRestricted = connectivityManager?.restrictBackgroundStatus ==
                ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
        )
    }

}
