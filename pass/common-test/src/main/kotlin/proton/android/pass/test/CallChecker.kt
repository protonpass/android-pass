package proton.android.pass.test

class CallChecker<T> {
    var isCalled = false
        private set

    var memory: T? = null
        private set

    fun call(value: T? = null) {
        memory = value
        isCalled = true
    }
}

