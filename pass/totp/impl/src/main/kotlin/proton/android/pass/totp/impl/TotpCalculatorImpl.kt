package proton.android.pass.totp.impl

import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import proton.android.pass.totp.api.TotpAlgorithm
import proton.android.pass.totp.api.TotpCalculator
import proton.android.pass.totp.api.TotpSpec
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TotpCalculatorImpl @Inject constructor(
    private val clock: Clock
) : TotpCalculator {
    override fun calculate(spec: TotpSpec): String {
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
}
