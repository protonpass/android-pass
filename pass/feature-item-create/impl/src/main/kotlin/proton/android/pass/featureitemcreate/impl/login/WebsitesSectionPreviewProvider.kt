package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class WebsitesSectionPreviewProvider(val withErrors: Boolean = false) :
    PreviewParameterProvider<WebsitesPreviewParameter> {
    override val values: Sequence<WebsitesPreviewParameter>
        get() = sequence {
            for (isEditAllowed in listOf(true, false)) {
                for (websiteList in websites) {
                    yield(
                        WebsitesPreviewParameter(
                            websites = websiteList,
                            websitesWithErrors = if (withErrors) {
                                websiteList.indices.toImmutableList()
                            } else {
                                persistentListOf()
                            },
                            isEditAllowed = isEditAllowed
                        )
                    )
                }
            }
        }

    private val websites = listOf(
        persistentListOf(),
        persistentListOf("https://one.website"),
        persistentListOf("https://one.website", "https://two.websites")
    )
}

data class WebsitesPreviewParameter(
    val websites: ImmutableList<String>,
    val websitesWithErrors: ImmutableList<Int>,
    val isEditAllowed: Boolean
)
