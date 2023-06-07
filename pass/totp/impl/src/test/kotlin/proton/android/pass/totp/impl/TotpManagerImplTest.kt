package proton.android.pass.totp.impl

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Test
import proton.android.pass.test.FixedClock
import proton.android.pass.totp.api.TotpAlgorithm
import proton.android.pass.totp.api.TotpDigits
import proton.android.pass.totp.api.TotpSpec

class TotpManagerImplTest {

    lateinit var instance: TotpManagerImpl

    @Before
    fun setup() {
        instance = TotpManagerImpl(FixedClock(Clock.System.now()))
    }

    @Test
    fun `generateUri removes trailing slashes from label`() {
        val label = "thisIsMyLabel"
        val labelWithTrailingSlashes = "$label///"
        val res = instance.generateUri(
            TotpSpec(
                secret = "SECRET",
                label = labelWithTrailingSlashes,
                algorithm = TotpAlgorithm.Sha1,
                digits = TotpDigits.Six,
                validPeriodSeconds = 30,
            )
        )

        assertThat(res).isEqualTo(
            "otpauth://totp/$label/?digits=6&algorithm=SHA1&period=30&secret=SECRET"
        )
    }

    @Test
    fun `parse and generateUri respects the format`() {
        val source = "otpauth://totp/testLabel/?digits=6&algorithm=SHA1&period=30&secret=SECRET"

        val res = instance.generateUri(instance.parse(source).getOrThrow())
        assertThat(res).isEqualTo(source)
    }

}
