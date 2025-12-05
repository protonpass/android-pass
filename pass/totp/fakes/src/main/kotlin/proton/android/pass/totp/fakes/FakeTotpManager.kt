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
import kotlinx.coroutines.flow.flowOf
import proton.android.pass.totp.api.TotpManager
import proton.android.pass.totp.api.TotpSpec
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeTotpManager @Inject constructor() : TotpManager {

    private var parseResult: Result<TotpSpec> = Result.failure(NotImplementedError())
    private var sanitisedEditResult = Result.success("")
    private val sanitisedSaveResultsQueue: Queue<Result<String>> = LinkedList()
    private var totpWrapper = TotpManager.TotpWrapper("", 0, 0)

    fun setParseResult(result: Result<TotpSpec>) {
        parseResult = result
    }

    fun setSanitisedEditResult(result: Result<String>) {
        sanitisedEditResult = result
    }

    fun addSanitisedSaveResult(result: Result<String>) {
        sanitisedSaveResultsQueue.add(result)
    }

    override fun observeCode(uri: String): Flow<TotpManager.TotpWrapper> = flowOf(totpWrapper)

    override fun parse(uri: String): Result<TotpSpec> = parseResult

    override fun sanitiseToEdit(uri: String): Result<String> = sanitisedEditResult

    override fun sanitiseToSave(originalUri: String, editedUri: String): Result<String> =
        sanitisedSaveResultsQueue.poll() ?: Result.success("")
}
