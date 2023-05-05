package proton.android.pass.common.api

@Suppress("SwallowedException")
fun isInstrumentedTest(): Boolean = try {
    Class.forName("proton.android.pass.test.HiltRunner")
    true
} catch (e: ClassNotFoundException) {
    false
}
