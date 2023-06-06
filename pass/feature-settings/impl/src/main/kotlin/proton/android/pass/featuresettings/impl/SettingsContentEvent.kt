package proton.android.pass.featuresettings.impl

sealed interface SettingsContentEvent {
    data class UseFaviconsChange(val value: Boolean) : SettingsContentEvent
    data class AllowScreenshotsChange(val value: Boolean) : SettingsContentEvent
    data class TelemetryChange(val value: Boolean) : SettingsContentEvent
    data class CrashReportChange(val value: Boolean) : SettingsContentEvent
    object SelectTheme : SettingsContentEvent
    object Clipboard : SettingsContentEvent
    object ViewLogs : SettingsContentEvent
    object ForceSync : SettingsContentEvent
    object Privacy : SettingsContentEvent
    object Terms : SettingsContentEvent
    object PrimaryVault : SettingsContentEvent
    object Up : SettingsContentEvent
}
