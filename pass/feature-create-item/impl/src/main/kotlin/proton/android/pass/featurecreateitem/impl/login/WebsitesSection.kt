package proton.android.pass.featurecreateitem.impl.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextTitle
import proton.android.pass.featurecreateitem.impl.R

@Composable
internal fun WebsitesSection(
    modifier: Modifier = Modifier,
    websites: ImmutableList<String>,
    focusLastWebsite: Boolean,
    isEditAllowed: Boolean,
    onWebsitesChange: OnWebsiteChange,
    doesWebsiteIndexHaveError: (Int) -> Boolean
) {
    ProtonTextTitle(
        title = stringResource(id = R.string.field_website_address_title),
        modifier = Modifier.padding(vertical = 8.dp)
    )

    // Only show the remove button if there is more than 1 website
    val shouldShowRemoveButton = websites.size > 1
    var isFocused: Boolean by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ProtonTheme.colors.backgroundSecondary)
            .border(
                width = if (isFocused) 1.dp else 0.dp,
                shape = RoundedCornerShape(8.dp),
                color = if (isFocused) ProtonTheme.colors.brandNorm else Color.Transparent
            )
    ) {
        val shouldShowAddWebsiteButton =
            websites.count() == 1 && websites.last().isNotEmpty() || websites.count() > 1

        val focusRequester = remember { FocusRequester() }
        websites.forEachIndexed { idx, value ->
            val textFieldModifier = if (idx < websites.count() - 1) {
                Modifier
            } else {
                Modifier.focusRequester(focusRequester)
            }
            ProtonTextField(
                modifier = textFieldModifier.fillMaxWidth(1.0f),
                isError = doesWebsiteIndexHaveError(idx),
                value = value,
                editable = isEditAllowed,
                onChange = { onWebsitesChange.onWebsiteValueChanged(it, idx) },
                onFocusChange = { isFocused = it },
                placeholder = stringResource(id = R.string.field_website_address_hint),
                trailingIcon = {
                    if (shouldShowRemoveButton) {
                        Icon(
                            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_minus_circle),
                            contentDescription = null,
                            tint = ProtonTheme.colors.iconNorm,
                            modifier = Modifier.clickable { onWebsitesChange.onRemoveWebsite(idx) }
                        )
                    }
                }
            )
            if (shouldShowAddWebsiteButton) {
                Divider()
            }
        }

        // If we receive focusLastWebsite, call requestFocus
        LaunchedEffect(focusLastWebsite) {
            if (focusLastWebsite) {
                focusRequester.requestFocus()
            }
        }

        AnimatedVisibility(shouldShowAddWebsiteButton) {
            val ableToAddNewWebsite = websites.last().isNotEmpty()
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = ableToAddNewWebsite,
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                ),
                contentPadding = PaddingValues(16.dp),
                onClick = { onWebsitesChange.onAddWebsite() },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    disabledBackgroundColor = Color.Transparent,
                    contentColor = ProtonTheme.colors.brandNorm,
                    disabledContentColor = ProtonTheme.colors.interactionDisabled
                )
            ) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_plus),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.field_website_add_another))
                Spacer(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

class ThemedWebsitesSectionPreviewProvider :
    ThemePairPreviewProvider<WebsitesPreviewParameter>(WebsitesSectionPreviewProvider())

@Preview
@Composable
fun WebsitesSectionPreview(
    @PreviewParameter(ThemedWebsitesSectionPreviewProvider::class) input: Pair<Boolean, WebsitesPreviewParameter>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            WebsitesSection(
                websites = input.second.websites.toImmutableList(),
                isEditAllowed = input.second.isEditAllowed,
                focusLastWebsite = false,
                onWebsitesChange = object : OnWebsiteChange {
                    override val onAddWebsite: () -> Unit
                        get() = {}
                    override val onRemoveWebsite: (Int) -> Unit
                        get() = {}
                    override val onWebsiteValueChanged: (String, Int) -> Unit
                        get() = { _, _ -> }
                },
                doesWebsiteIndexHaveError = { false }
            )
        }
    }
}
