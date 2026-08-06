/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.data.impl.usecases

import android.content.Context
import android.content.res.Resources
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.data.api.usecases.GetPublicSuffixList
import proton.android.pass.data.api.usecases.PublicSuffixRules
import proton.android.pass.data.impl.R
import proton.android.pass.log.api.PassLogger
import java.io.IOException
import javax.inject.Inject

class GetPublicSuffixListImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : GetPublicSuffixList {

    private var suffixes: PublicSuffixRules? = null

    override fun invoke(): Result<PublicSuffixRules> {
        suffixes?.let { return Result.success(it) }
        return loadSuffixes().onSuccess { suffixes = it }
    }

    private fun loadSuffixes(): Result<PublicSuffixRules> = try {
        val contents = context.resources
            .openRawResource(R.raw.public_suffix_list)
            .bufferedReader()
            .use { it.readText() }

        val exact = mutableSetOf<String>()
        val wildcardBases = mutableSetOf<String>()
        val exceptions = mutableSetOf<String>()
        contents.lineSequence().filter { it.isNotBlank() }.forEach { line ->
            when {
                line.startsWith("*.") -> wildcardBases.add(line.removePrefix("*."))
                line.startsWith("!") -> exceptions.add(line.removePrefix("!"))
                else -> exact.add(line)
            }
        }
        Result.success(PublicSuffixRules(exact, wildcardBases, exceptions))
    } catch (e: IOException) {
        PassLogger.e(TAG, e, "Error reading public_suffix_list")
        Result.failure(e)
    } catch (e: Resources.NotFoundException) {
        PassLogger.e(TAG, e, "Error reading public_suffix_list")
        Result.failure(e)
    }

    companion object {
        private const val TAG = "GetPublicSuffixListImpl"
    }
}



