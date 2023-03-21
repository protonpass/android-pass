package proton.android.pass.telemetry.api

enum class EventItemType(val itemTypeName: String) {
    Login("login"),
    Note("note"),
    Alias("alias"),
    Password("password")
}

enum class AutofillTriggerSource(val source: String) {
    Source("source"),
    App("app")
}

sealed class TelemetryEvent(val eventName: String) {

    data class ItemCreate(val itemType: EventItemType) : TelemetryEvent("item.creation") {
        override fun dimensions(): Map<String, String> = mapOf("type" to itemType.itemTypeName)
    }
    data class ItemUpdate(val itemType: EventItemType) : TelemetryEvent("item.update") {
        override fun dimensions(): Map<String, String> = mapOf("type" to itemType.itemTypeName)
    }
    data class ItemDelete(val itemType: EventItemType) : TelemetryEvent("item.deletion") {
        override fun dimensions(): Map<String, String> = mapOf("type" to itemType.itemTypeName)
    }
    data class ItemRead(val itemType: EventItemType) : TelemetryEvent("item.read") {
        override fun dimensions(): Map<String, String> = mapOf("type" to itemType.itemTypeName)
    }

    object AutoSuggestAliasCreated : TelemetryEvent("autosuggest.alias_created")
    object AutosaveDone : TelemetryEvent("autosave.done")
    object AutosaveDisplay : TelemetryEvent("autosave.display")

    data class AutofillDisplayed(val source: AutofillTriggerSource) : TelemetryEvent("autofill.display") {
        override fun dimensions(): Map<String, String> = mapOf("location" to source.source)
    }
    data class AutofillDone(val source: AutofillTriggerSource) : TelemetryEvent("autofill.triggered") {
        override fun dimensions(): Map<String, String> = mapOf("location" to source.source)
    }

    object SearchTriggered : TelemetryEvent("search.triggered")
    object SearchItemClicked : TelemetryEvent("search.click")

    open fun dimensions(): Map<String, String> = emptyMap()
}
