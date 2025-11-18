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

package proton.android.pass.image.impl

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import proton.android.pass.image.api.ClearIconCache
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class ClearIconCacheImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ClearIconCache {
    override suspend fun invoke() = withContext(Dispatchers.IO) {
        PassLogger.i(TAG, "Removing icon cache")
        val cacheDir = CacheUtils.cacheDir(context)
        if (cacheDir.deleteRecursively()) {
            PassLogger.i(TAG, "Removed icon cache")
        } else {
            PassLogger.w(TAG, "Could not remove icon cache")
        }
    }

    companion object {
        private const val TAG = "ClearIconCacheImpl"
    }

}
