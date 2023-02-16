package proton.android.pass.test

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

object TestUtils {
    fun randomString(length: Int = 10): String {
        val dict = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var res = ""
        while (res.length < length) {
            res += dict.random()
        }
        return res
    }

    fun <T> testFlow() = MutableSharedFlow<T>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1
    )
}
