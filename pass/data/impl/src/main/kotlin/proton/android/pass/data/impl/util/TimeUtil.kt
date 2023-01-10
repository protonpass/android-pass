package proton.android.pass.data.impl.util

import java.time.OffsetDateTime
import java.time.ZoneOffset

object TimeUtil {
    fun getNowUtc(): Long = OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond()
}
