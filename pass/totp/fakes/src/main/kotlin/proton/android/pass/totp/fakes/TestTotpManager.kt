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

package proton.android.pass.totp.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import proton.android.pass.totp.api.TotpManager
import proton.android.pass.totp.api.TotpSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestTotpManager @Inject constructor() : TotpManager {

    private var parseResult: Result<TotpSpec> = Result.failure(NotImplementedError())
    private var generatedUri = ""

    fun setParseResult(result: Result<TotpSpec>) {
        parseResult = result
    }

    fun setGeneratedUri(uri: String) {
        generatedUri = uri
    }

    override fun generateUri(spec: TotpSpec): String = generatedUri

    override fun generateUriWithDefaults(secret: String): Result<String> = Result.success("")

    override fun observeCode(spec: TotpSpec): Flow<TotpManager.TotpWrapper> = emptyFlow()

    override fun parse(uri: String): Result<TotpSpec> = parseResult
}
