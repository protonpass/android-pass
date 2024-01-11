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

package proton.android.pass.totp.impl

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import proton.android.pass.totp.api.TotpManager
import proton.android.pass.totp.api.TotpSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.floor

@Singleton
class TotpManagerImpl @Inject constructor(
    private val clock: Clock,
    private val totpUriParser: TotpUriParser,
    private val totpUriSanitiser: TotpUriSanitiser,
    private val totpTokenGenerator: TotpTokenGenerator
) : TotpManager {

    override fun observeCode(uri: String): Flow<TotpManager.TotpWrapper> = flow {
        while (currentCoroutineContext().isActive) {
            val timestamp = clock.now().toJavaInstant()
            val generated = totpTokenGenerator.generate(uri, timestamp.epochSecond.toULong())
                .getOrThrow()
            val code = generated.token
            val spec = generated.spec

            val remainingSeconds =
                spec.validPeriodSeconds - floor(timestamp.epochSecond.toDouble()) % spec.validPeriodSeconds
            val wrapper = TotpManager.TotpWrapper(
                code = code,
                remainingSeconds = remainingSeconds.toInt(),
                totalSeconds = spec.validPeriodSeconds
            )
            emit(wrapper)
            delay(ONE_SECOND_MILLISECONDS)
        }
    }

    override fun parse(uri: String): Result<TotpSpec> = totpUriParser.parse(uri)

    override fun sanitiseToEdit(uri: String): Result<String> = totpUriSanitiser.sanitiseToEdit(uri)

    override fun sanitiseToSave(originalUri: String, editedUri: String): Result<String> =
        totpUriSanitiser.sanitiseToSave(originalUri, editedUri)

    companion object {
        private const val ONE_SECOND_MILLISECONDS = 1000L
    }
}
