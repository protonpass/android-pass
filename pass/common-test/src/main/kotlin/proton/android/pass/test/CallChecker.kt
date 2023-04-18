package proton.android.pass.test

class CallChecker {
    var isCalled = false
        private set

    fun call() {
        isCalled = true
    }
}

