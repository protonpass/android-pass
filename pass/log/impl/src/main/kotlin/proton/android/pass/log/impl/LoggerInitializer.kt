/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.log.impl

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.LocaleList
import android.os.StatFs
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import me.proton.core.util.android.sentry.TimberLogger
import me.proton.core.util.kotlin.CoreLogger
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.log.api.PassLogger
import proton.android.pass.tracing.impl.SentryInitializer
import timber.log.Timber
import java.text.DecimalFormat

class LoggerInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            LoggerInitializerEntryPoint::class.java
        )

        if (entryPoint.appConfig().isDebug) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(FileLoggingTree(context))
        deviceInfo(context, entryPoint.appConfig())

        // Forward Core Logs to Timber, using TimberLogger.
        CoreLogger.set(TimberLogger)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(
        SentryInitializer::class.java
    )

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LoggerInitializerEntryPoint {
        fun appConfig(): AppConfig
    }
}

private fun deviceInfo(context: Context, appConfig: AppConfig) {
    val memory = getMemory(context)
    val storage = getStorage()
    PassLogger.i(TAG, "-----------------------------------------")
    PassLogger.i(
        TAG,
        "OS:          Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    )
    PassLogger.i(TAG, "VERSION:     ${appConfig.versionName}")
    PassLogger.i(TAG, "DEVICE:      ${Build.MANUFACTURER} ${Build.MODEL}")
    PassLogger.i(TAG, "FINGERPRINT: ${Build.FINGERPRINT}")
    PassLogger.i(TAG, "ABI:         ${Build.SUPPORTED_ABIS.joinToString(",")}")
    PassLogger.i(TAG, "LOCALE:      ${LocaleList.getDefault().toLanguageTags()}")
    PassLogger.i(TAG, "MEMORY:      $memory")
    PassLogger.i(TAG, "STORAGE:     $storage")
    PassLogger.i(TAG, "-----------------------------------------")
}

private fun getStorage(): String {
    val free = freeStorage()
    val total = totalStorage()
    return "Free: ${bytesToHuman(free)} | Total: ${bytesToHuman(total)}"
}

private fun getMemory(context: Context): String {
    val mi = ActivityManager.MemoryInfo()
    val activityManager = context.getSystemService(ActivityManager::class.java)
        ?: return "UNAVAILABLE"
    activityManager.getMemoryInfo(mi)

    val availableMegs: Double = mi.availMem.toDouble() / 0x100000L
    val totalMegs: Double = mi.totalMem.toDouble() / 0x100000L
    val fractionAvail: Double = mi.availMem.toDouble() / mi.totalMem.toDouble()
    val percentAvail: Double = fractionAvail * 100

    return "Available: ${floatForm(availableMegs)} MB / ${floatForm(totalMegs)} MB (${floatForm(percentAvail)}% used)"
}

private fun totalStorage(): Long {
    val statFs = StatFs(Environment.getRootDirectory().absolutePath)
    return statFs.blockCountLong * statFs.blockSizeLong
}

private fun freeStorage(): Long {
    val statFs = StatFs(Environment.getRootDirectory().absolutePath)
    return statFs.freeBlocksLong * statFs.blockSizeLong
}

private fun floatForm(d: Double) = DecimalFormat("#.##").format(d)

private fun bytesToHuman(size: Long): String {
    val kb = (1 * 1024).toLong()
    val mb = kb * 1024
    val gb = mb * 1024
    val tb = gb * 1024
    val pb = tb * 1024
    val eb = pb * 1024

    return when {
        size < kb -> floatForm(size.toDouble()) + " byte"
        size in kb until mb -> floatForm(size.toDouble() / kb) + " KB"
        size in mb until gb -> floatForm(size.toDouble() / mb) + " MB"
        size in gb until tb -> floatForm(size.toDouble() / gb) + " GB"
        size in tb until pb -> floatForm(size.toDouble() / tb) + " TB"
        size in pb until eb -> floatForm(size.toDouble() / pb) + " PB"
        else -> floatForm(size.toDouble() / eb) + " EB"
    }
}

private const val TAG = "DEVICE_INFO"
