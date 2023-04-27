package proton.android.pass.preferences

import me.proton.android.pass.preferences.BooleanPrefProto
import me.proton.android.pass.preferences.LockAppPrefProto

fun Boolean.toBooleanPrefProto() = if (this) {
    BooleanPrefProto.BOOLEAN_PREFERENCE_TRUE
} else {
    BooleanPrefProto.BOOLEAN_PREFERENCE_FALSE
}

fun fromBooleanPrefProto(pref: BooleanPrefProto, default: Boolean = false) =
    when (pref) {
        BooleanPrefProto.BOOLEAN_PREFERENCE_TRUE -> true
        BooleanPrefProto.BOOLEAN_PREFERENCE_FALSE -> false
        else -> default
    }

fun AppLockPreference.toProto() = when (this) {
    AppLockPreference.Immediately -> LockAppPrefProto.LOCK_APP_IMMEDIATELY
    AppLockPreference.Never -> LockAppPrefProto.LOCK_APP_NEVER
    AppLockPreference.InOneMinute -> LockAppPrefProto.LOCK_APP_IN_ONE_MINUTE
    AppLockPreference.InTwoMinutes -> LockAppPrefProto.LOCK_APP_IN_TWO_MINUTES
    AppLockPreference.InFiveMinutes -> LockAppPrefProto.LOCK_APP_IN_FIVE_MINUTES
    AppLockPreference.InTenMinutes -> LockAppPrefProto.LOCK_APP_IN_TEN_MINUTES
    AppLockPreference.InOneHour -> LockAppPrefProto.LOCK_APP_IN_ONE_HOUR
    AppLockPreference.InFourHours -> LockAppPrefProto.LOCK_APP_IN_FOUR_HOURS
}

fun LockAppPrefProto.toValue(default: AppLockPreference) = when (this) {
    LockAppPrefProto.LOCK_APP_IMMEDIATELY -> AppLockPreference.Immediately
    LockAppPrefProto.LOCK_APP_NEVER -> AppLockPreference.Never
    LockAppPrefProto.LOCK_APP_IN_ONE_MINUTE -> AppLockPreference.InOneMinute
    LockAppPrefProto.LOCK_APP_IN_TWO_MINUTES -> AppLockPreference.InTwoMinutes
    LockAppPrefProto.LOCK_APP_IN_FIVE_MINUTES -> AppLockPreference.InFiveMinutes
    LockAppPrefProto.LOCK_APP_IN_TEN_MINUTES -> AppLockPreference.InTenMinutes
    LockAppPrefProto.LOCK_APP_IN_ONE_HOUR -> AppLockPreference.InOneHour
    LockAppPrefProto.LOCK_APP_IN_FOUR_HOURS -> AppLockPreference.InFourHours
    else -> default
}
