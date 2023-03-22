package proton.android.pass.telemetry.api

enum class EventItemType(val itemTypeName: String) {
    Login("login"),
    Note("note"),
    Alias("alias"),
    Password("password")
}

@Suppress("UnnecessaryAbstractClass")
abstract class TelemetryEvent(val eventName: String) {
    open fun dimensions(): Map<String, String> = emptyMap()
}
