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

import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.OtpAuthUriBuilder
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.apache.commons.codec.binary.Base32
import proton.android.pass.totp.api.TotpAlgorithm
import proton.android.pass.totp.api.TotpManager
import proton.android.pass.totp.api.TotpSpec
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TotpManagerImpl @Inject constructor(
    private val clock: Clock
) : TotpManager {

    override fun generateUri(spec: TotpSpec): String {
        val secret = sanitizeSecret(spec.secret).encodeToByteArray()
        val builder = OtpAuthUriBuilder.forTotp(secret)

        val labelWithoutTrailingSlashes = spec.label.replace(TRAILING_SLASH_REGEX, "")

        builder.label(labelWithoutTrailingSlashes, issuer = null)
        spec.issuer.value()?.let { builder.issuer(it) }
        builder.digits(spec.digits.digits)
        val algorithm = when (spec.algorithm) {
            TotpAlgorithm.Sha1 -> HmacAlgorithm.SHA1
            TotpAlgorithm.Sha256 -> HmacAlgorithm.SHA256
            TotpAlgorithm.Sha512 -> HmacAlgorithm.SHA512
        }
        builder.algorithm(algorithm)
        builder.period(spec.validPeriodSeconds.toLong(), TimeUnit.SECONDS)
        return builder.buildToString()
    }

    @Suppress("MagicNumber")
    override fun generateUriWithDefaults(secret: String): Result<String> {
        val sanitized = sanitizeSecret(secret)
        return if (sanitized.matches(Regex("^[a-zA-Z0-9]+"))) {
            val uri = OtpAuthUriBuilder.forTotp(sanitized.encodeToByteArray())
                .digits(6)
                .algorithm(HmacAlgorithm.SHA1)
                .period(30, TimeUnit.SECONDS)
                .label(OtpUriParser.DEFAULT_LABEL, null)
                .buildToString()
            Result.success(uri)
        } else {
            Result.failure(IllegalArgumentException("Secret must be base32 encoded"))
        }
    }

    override fun observeCode(spec: TotpSpec): Flow<TotpManager.TotpWrapper> {
        val config = TimeBasedOneTimePasswordConfig(
            timeStep = spec.validPeriodSeconds.toLong(),
            timeStepUnit = TimeUnit.SECONDS,
            codeDigits = spec.digits.digits,
            hmacAlgorithm = when (spec.algorithm) {
                TotpAlgorithm.Sha1 -> HmacAlgorithm.SHA1
                TotpAlgorithm.Sha256 -> HmacAlgorithm.SHA256
                TotpAlgorithm.Sha512 -> HmacAlgorithm.SHA512
            }
        )
        val sanitizedSecret = sanitizeSecret(spec.secret)

        val generator = TimeBasedOneTimePasswordGenerator(Base32().decode(sanitizedSecret), config)
        return flow {
            while (currentCoroutineContext().isActive) {
                val startTimestamp = clock.now().toJavaInstant().toEpochMilli()
                val code = generator.generate(startTimestamp)
                val counter = generator.counter(startTimestamp)
                val endEpochMillis = generator.timeslotStart(counter + 1) - 1
                var now = clock.now().toJavaInstant().toEpochMilli()
                while (generator.isValid(code, now)) {
                    val millisValid = endEpochMillis - now
                    val wrapper = TotpManager.TotpWrapper(
                        code = code,
                        remainingSeconds = (millisValid / ONE_SECOND_MILLISECONDS).toInt(),
                        totalSeconds = spec.validPeriodSeconds
                    )
                    emit(wrapper)
                    delay(ONE_SECOND_MILLISECONDS)
                    now = clock.now().toJavaInstant().toEpochMilli()
                }
            }
        }
    }

    override fun parse(uri: String): Result<TotpSpec> = OtpUriParser.parse(uri)

    private fun sanitizeSecret(secret: String) = secret.replace(" ", "")

    companion object {
        private const val ONE_SECOND_MILLISECONDS = 1000L
        private val TRAILING_SLASH_REGEX = Regex("/+$")
    }
}
