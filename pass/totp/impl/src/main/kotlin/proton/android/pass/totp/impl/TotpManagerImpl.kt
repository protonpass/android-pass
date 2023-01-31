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
        val generator = TimeBasedOneTimePasswordGenerator(Base32().decode(spec.secret), config)
        return flow {
            while (currentCoroutineContext().isActive) {
                val startTimestamp = clock.now().toJavaInstant().toEpochMilli()
                val code = generator.generate(startTimestamp)
                val counter = generator.counter(startTimestamp)
                val endEpochMillis = generator.timeslotStart(counter + 1) - 1
                var now = clock.now().toJavaInstant().toEpochMilli()
                while (generator.isValid(code, now)) {
                    val millisValid = endEpochMillis - now
                    emit(code to (millisValid / ONE_SECOND_MILLISECONDS).toInt())
                    delay(ONE_SECOND_MILLISECONDS)
                    now = clock.now().toJavaInstant().toEpochMilli()
                }
            }
        }
    }

    override fun parse(uri: String): Result<TotpSpec> = OtpUriParser.parse(uri)

    companion object {
        private const val ONE_SECOND_MILLISECONDS = 1000L
    }
}
