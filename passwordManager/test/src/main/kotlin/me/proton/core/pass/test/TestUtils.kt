package me.proton.core.pass.test

object TestUtils {
    fun randomString(length: Int = 10): String {
        val dict = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var res = ""
        while (res.length < length) {
            res += dict.random()
        }
        return res
    }
}
