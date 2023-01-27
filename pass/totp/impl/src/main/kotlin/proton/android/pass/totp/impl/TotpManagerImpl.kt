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
import proton.android.pass.common.api.Result
import proton.android.pass.totp.api.TotpAlgorithm
import proton.android.pass.totp.api.TotpManager
import proton.android.pass.totp.api.TotpSpec
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TotpManagerImpl @Inject constructor(
    private val clock: Clock
) : TotpManager {

    override fun generateUri(spec: TotpSpec): String {
        val builder = OtpAuthUriBuilder.forTotp(spec.secret.encodeToByteArray())
        builder.label(spec.label, spec.issuer.value())
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

    override fun calculateCode(spec: TotpSpec): String {
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

        val generator = TimeBasedOneTimePasswordGenerator(spec.secret.encodeToByteArray(), config)
        return generator.generate(clock.now().toJavaInstant())
    }

    override fun observeCode(spec: TotpSpec): Flow<Pair<String, Int>> {
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
        val generator = TimeBasedOneTimePasswordGenerator(spec.secret.encodeToByteArray(), config)
        return flow {
            while (currentCoroutineContext().isActive) {
                val timestamp = clock.now().toJavaInstant().toEpochMilli()
                val code = generator.generate(timestamp)
                val counter = generator.counter()
                val endEpochMillis = generator.timeslotStart(counter + 1) - 1
                while (generator.isValid(code)) {
                    val millisValid = endEpochMillis - clock.now().toJavaInstant().toEpochMilli()
                    emit(code to (millisValid / ONE_SECOND_MILLISECONDS).toInt())
                    delay(ONE_SECOND_MILLISECONDS)
                }
            }
        }
    }

    override fun parse(uri: String): Result<TotpSpec> = OtpUriParser.parse(uri)

    companion object {
        private const val ONE_SECOND_MILLISECONDS = 1000L
    }
}
