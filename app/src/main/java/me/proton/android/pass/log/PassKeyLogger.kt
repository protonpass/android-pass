package me.proton.android.pass.log

import android.os.Build
import android.os.LocaleList
import java.util.Locale
import me.proton.core.util.kotlin.Logger
import me.proton.core.util.kotlin.LoggerLogTag
import timber.log.Timber

object PassKeyLogger : Logger {
    override fun v(tag: String, message: String) = Timber.tag(tag).v(message)
    override fun v(tag: String, e: Throwable, message: String) = Timber.tag(tag).v(e, message)
    override fun d(tag: String, message: String) = Timber.tag(tag).d(message)
    override fun d(tag: String, e: Throwable, message: String) = Timber.tag(tag).d(e, message)
    override fun i(tag: String, message: String) = Timber.tag(tag).i(message)
    override fun i(tag: String, e: Throwable, message: String) = Timber.tag(tag).i(e, message)
    override fun e(tag: String, e: Throwable) = Timber.tag(tag).e(e)
    override fun e(tag: String, e: Throwable, message: String) = Timber.tag(tag).e(e, message)
    override fun log(tag: LoggerLogTag, message: String) = i(tag.name, message)
}

fun Logger.v(message: String) = v(AppLogTag.DEFAULT, message)
fun Logger.d(message: String) = d(AppLogTag.DEFAULT, message)
fun Logger.i(message: String) = i(AppLogTag.DEFAULT, message)
fun Logger.e(throwable: Throwable) = e(AppLogTag.DEFAULT, throwable)

fun Logger.deviceInfo() {
    i("-----------------------------------------")
    i("OS:          Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
    i("DEVICE:      ${Build.MANUFACTURER} ${Build.MODEL}")
    i("FINGERPRINT: ${Build.FINGERPRINT}")
    i("ABI:         ${Build.SUPPORTED_ABIS.joinToString(",")}")
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        i("LOCALE:      ${Locale.getDefault().toLanguageTag()}")
    } else {
        i("LOCALE:      ${LocaleList.getDefault().toLanguageTags()}")
    }
    i("-----------------------------------------")
}
