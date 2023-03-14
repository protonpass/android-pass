package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class WebsitesSectionPreviewProvider : PreviewParameterProvider<WebsitesPreviewParameter> {
    override val values: Sequence<WebsitesPreviewParameter>
        get() = sequence {
            for (isEditAllowed in listOf(true, false)) {
                for (websiteList in websites) {
                    yield(
                        WebsitesPreviewParameter(
                            websites = websiteList,
                            isEditAllowed = isEditAllowed
                        )
                    )
                }
            }
        }

    private val websites = listOf(
        emptyList(),
        listOf("https://one.website"),
        listOf("https://one.website", "https://two.websites")
    )
}

data class WebsitesPreviewParameter(
    val websites: List<String>,
    val isEditAllowed: Boolean
)
