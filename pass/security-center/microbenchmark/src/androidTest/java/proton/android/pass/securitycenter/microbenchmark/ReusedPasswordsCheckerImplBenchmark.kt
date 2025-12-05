package proton.android.pass.securitycenter.microbenchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.domain.Item
import proton.android.pass.securitycenter.impl.checkers.RepeatedPasswordCheckerImpl
import proton.android.pass.test.TestUtils
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestItemType

@RunWith(AndroidJUnit4::class)
class ReusedPasswordsCheckerImplBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private lateinit var instance: RepeatedPasswordCheckerImpl
    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider

    @Before
    fun setup() {
        encryptionContextProvider = FakeEncryptionContextProvider()
        instance = RepeatedPasswordCheckerImpl(
            encryptionContextProvider = encryptionContextProvider
        )
    }

    @Test
    fun checkRepeatedPasswords() {
        val items = generateDataset(numPasswords = 10, numItems = 10_000)
        benchmarkRule.measureRepeated {
            runBlocking(Dispatchers.Default) {
                instance.invoke(items)
            }
        }
    }

    private fun generateDataset(numPasswords: Int, numItems: Int): List<Item> {
        val passwords = encryptionContextProvider.withEncryptionContext {
            (0 until numPasswords).map {
                encrypt(TestUtils.randomString())
            }
        }

        return (0 until numItems).map { idx ->
            TestItem.random(TestItemType.login(password = passwords[idx % passwords.size]))
        }
    }
}
