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

package proton.android.pass.biometry

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

interface BootCountRetriever {
    fun get(): Long
}

class BootCountRetrieverImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : BootCountRetriever {

    override fun get() = runCatching {
        Settings.Global
            .getInt(context.contentResolver, Settings.Global.BOOT_COUNT)
            .toLong()
    }.onFailure {
        PassLogger.e(TAG, it, "Error getting boot count")
    }.getOrDefault(-1)

    companion object {
        private const val TAG = "BootCountRetrieverImpl"
    }

}
