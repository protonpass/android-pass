package proton.android.pass.featureprofile.impl.applock

import proton.android.pass.preferences.AppLockPreference

sealed interface AppLockEvent {
    object OnChanged : AppLockEvent
    object Unknown : AppLockEvent
}

data class AppLockUiState(
    val items: List<AppLockPreference>,
    val selected: AppLockPreference,
    val event: AppLockEvent
) {
    companion object {
        val Initial = AppLockUiState(
            items = allPreferences,
            selected = AppLockPreference.InTwoMinutes,
            event = AppLockEvent.Unknown
        )
    }
}

internal val allPreferences: List<AppLockPreference> = listOf(
    AppLockPreference.Immediately,
    AppLockPreference.InOneMinute,
    AppLockPreference.InTwoMinutes,
    AppLockPreference.InFiveMinutes,
    AppLockPreference.InTenMinutes,
    AppLockPreference.InOneHour,
    AppLockPreference.InFourHours,
)
