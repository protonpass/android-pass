package proton.android.pass.uitest.suite

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import proton.android.pass.uitest.flow.SignUpFlowTest

@RunWith(Suite::class)
@SuiteClasses(
    SignUpFlowTest::class
)
class SmokeSuite
