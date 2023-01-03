package me.proton.android.pass.log.api

import me.proton.core.network.domain.ApiException
import me.proton.core.util.kotlin.Logger
import me.proton.core.util.kotlin.LoggerLogTag
import timber.log.Timber

object PassLogger : Logger {
    override fun v(tag: String, message: String) = Timber.tag(tag).v(message)
    override fun v(tag: String, e: Throwable, message: String) = Timber.tag(tag).v(e, message)
    override fun d(tag: String, message: String) = Timber.tag(tag).d(message)
    override fun d(tag: String, e: Throwable, message: String) = Timber.tag(tag).d(e, message)
    override fun i(tag: String, message: String) = Timber.tag(tag).i(message)
    override fun i(tag: String, e: Throwable, message: String) = Timber.tag(tag).i(e, message)
    fun w(tag: String, e: Throwable) = Timber.tag(tag).w(e)
    fun w(tag: String, e: Throwable, message: String) = Timber.tag(tag).w(e, message)
    override fun e(tag: String, e: Throwable) = Timber.tag(tag).e(e)
    override fun e(tag: String, e: Throwable, message: String) {
        // Temporarily change priority of ApiException errors
        if (e is ApiException) {
            Timber.tag(tag).i(e, message)
        } else {
            Timber.tag(tag).e(e, message)
        }
    }

    override fun log(tag: LoggerLogTag, message: String) = i(tag.name, message)
}

fun Logger.v(message: String) = v("app", message)
fun Logger.d(message: String) = d("app", message)
fun Logger.i(message: String) = i("app", message)
fun Logger.e(throwable: Throwable) = e("app", throwable)
