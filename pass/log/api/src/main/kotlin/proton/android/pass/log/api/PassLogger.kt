package proton.android.pass.log.api

import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
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
    fun w(tag: String, message: String) = Timber.tag(tag).w(message)
    override fun e(tag: String, e: Throwable) = Timber.tag(tag).e(e)
    override fun e(tag: String, e: Throwable, message: String) {
        if (e is ApiException) {
            if (e.error is ApiResult.Error.Connection) {
                Timber.tag(tag).w(e, message)
                return
            }
        }
        Timber.tag(tag).e(e, message)
    }

    override fun log(tag: LoggerLogTag, message: String) = i(tag.name, message)
}
