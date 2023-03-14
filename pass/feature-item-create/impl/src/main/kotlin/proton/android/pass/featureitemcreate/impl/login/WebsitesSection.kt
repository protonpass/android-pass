package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featureitemcreate.impl.R

@Composable
internal fun WebsitesSection(
    modifier: Modifier = Modifier,
    websites: ImmutableList<String>,
    focusLastWebsite: Boolean,
    isEditAllowed: Boolean,
    onWebsitesChange: OnWebsiteChange,
    doesWebsiteIndexHaveError: (Int) -> Boolean
) {
    var isFocused: Boolean by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    Row(
        modifier = modifier
            .roundedContainer(ProtonTheme.colors.separatorNorm)
            .padding(0.dp, 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(10.dp, 0.dp, 0.dp, 0.dp),
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_earth),
            contentDescription = "",
            tint = ProtonTheme.colors.iconWeak
        )
        Column {
            websites.forEachIndexed { idx, value ->
                val textFieldModifier = if (idx < websites.count() - 1) {
                    Modifier
                } else {
                    Modifier.focusRequester(focusRequester)
                }
                ProtonTextField(
                    modifier = textFieldModifier,
                    isError = doesWebsiteIndexHaveError(idx),
                    value = value,
                    editable = isEditAllowed,
                    textStyle = ProtonTheme.typography.default(isEditAllowed),
                    onChange = {
                        if (it.isBlank() && websites.size > 1) {
                            onWebsitesChange.onRemoveWebsite(idx)
                        } else {
                            onWebsitesChange.onWebsiteValueChanged(it, idx)
                        }
                    },
                    onFocusChange = { isFocused = it },
                    label = if (idx == 0) {
                        { ProtonTextFieldLabel(text = stringResource(id = R.string.field_website_address_title)) }
                    } else {
                        null
                    },
                    placeholder = {
                        ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.field_website_address_hint))
                    },
                    trailingIcon = if (websites[idx].isNotEmpty() && isEditAllowed) {
                        {
                            SmallCrossIconButton {
                                if (websites[idx].isNotBlank() && websites.size > 1) {
                                    onWebsitesChange.onRemoveWebsite(idx)
                                } else {
                                    onWebsitesChange.onWebsiteValueChanged("", idx)
                                }
                            }
                        }
                    } else {
                        null
                    }
                )
            }

            // If we receive focusLastWebsite, call requestFocus
            LaunchedEffect(focusLastWebsite) {
                if (focusLastWebsite) {
                    focusRequester.requestFocus()
                }
            }

            val shouldShowAddWebsiteButton = (
                websites.count() == 1 && websites.last()
                    .isNotEmpty() || websites.count() > 1
                ) && isEditAllowed
            AnimatedVisibility(shouldShowAddWebsiteButton) {
                val ableToAddNewWebsite = websites.lastOrNull()?.isNotEmpty() ?: false
                Button(
                    enabled = ableToAddNewWebsite,
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp
                    ),
                    contentPadding = PaddingValues(0.dp),
                    onClick = { onWebsitesChange.onAddWebsite() },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                        disabledBackgroundColor = Color.Transparent,
                        contentColor = ProtonTheme.colors.brandNorm,
                        disabledContentColor = ProtonTheme.colors.interactionDisabled
                    )
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_plus),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = stringResource(R.string.field_website_add_another),
                        style = ProtonTheme.typography.default,
                        color = ProtonTheme.colors.brandNorm
                    )
                }
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
    PassTheme(isDark = input.first) {
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
