package proton.android.pass.featureitemcreate.impl.login

sealed interface WebsiteSectionEvent {
    data class WebsiteValueChanged(
        val value: String,
        val index: Int
    ) : WebsiteSectionEvent

    object AddWebsite : WebsiteSectionEvent
    data class RemoveWebsite(val index: Int) : WebsiteSectionEvent
}
