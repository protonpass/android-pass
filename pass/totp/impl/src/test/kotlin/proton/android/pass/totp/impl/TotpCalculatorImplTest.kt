package proton.android.pass.totp.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.test.FixedClock
import proton.android.pass.totp.api.TotpAlgorithm
import proton.android.pass.totp.api.TotpDigits
import proton.android.pass.totp.api.TotpSpec

class TotpCalculatorImplTest {

    private lateinit var instance: TotpManagerImpl

    @Before
    fun setup() {
        instance = TotpManagerImpl(FixedClock(Instant.fromEpochMilliseconds(TIMESTAMP)))
    }

    @Test
    fun `can generate the correct code`() = runTest {
        val spec = TotpSpec(
            secret = "ORUGS43JON2GQZLTMVRXEZLU", // thisisthesecret
            label = "someRandomLabel",
            issuer = "issuer".some(),
            algorithm = TotpAlgorithm.Sha256,
            digits = TotpDigits.Eight,
            validPeriodSeconds = 20
        )

        instance.observeCode(spec)
            .test {
                assertThat(awaitItem().code).isEqualTo("86058600")
            }
    }

    @Test
    fun `reference qr code`() = runTest {
        val spec = TotpSpec(
            secret = "ORUGS43JON2GQZLTMVRXEZLU", // thisisthesecret
            label = "thisisthelabel",
            issuer = None,
            algorithm = TotpAlgorithm.Sha1,
            digits = TotpDigits.Six,
            validPeriodSeconds = 10
        )
        instance.observeCode(spec)
            .test {
                assertThat(awaitItem().code).isEqualTo("846277")
            }
    }

    companion object {
        @Suppress("UnderscoresInNumericLiterals")
        private const val TIMESTAMP = 1673941666206

    }
}
