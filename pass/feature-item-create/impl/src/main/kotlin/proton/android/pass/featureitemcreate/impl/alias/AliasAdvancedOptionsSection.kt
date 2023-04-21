package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.container.roundedContainerStrong
import proton.android.pass.composecomponents.impl.form.ChevronDownIcon
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featureitemcreate.impl.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AliasAdvancedOptionsSection(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    prefix: String,
    suffix: AliasSuffixUiModel?,
    isError: Boolean,
    isBottomSheet: Boolean,
    canSelectSuffix: Boolean,
    onPrefixChanged: (String) -> Unit,
    onSuffixClicked: () -> Unit
) {
    val keyboardManager = LocalSoftwareKeyboardController.current
    Column(
        modifier = modifier
            .applyIf(
                condition = isBottomSheet,
                ifTrue = { roundedContainerStrong() },
                ifFalse = { roundedContainerNorm() }
            )
    ) {
        ProtonTextField(
            modifier = modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp),
            textStyle = ProtonTheme.typography.defaultNorm,
            label = {
                ProtonTextFieldLabel(
                    text = stringResource(id = R.string.field_alias_prefix),
                    isError = isError
                )
            },
            value = prefix,
            onChange = onPrefixChanged,
            singleLine = true,
            moveToNextOnEnter = !isBottomSheet,
            onDoneClick = {
                if (isBottomSheet) {
                    keyboardManager?.hide()
                }
            },
            trailingIcon = if (prefix.isNotBlank() && enabled) {
                { SmallCrossIconButton { onPrefixChanged("") } }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None)
        )

        Divider(color = PassTheme.colors.inputBorderNorm)

        val aliasText = suffix?.suffix ?: ""
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onSuffixClicked, enabled = canSelectSuffix)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                ProtonTextFieldLabel(text = stringResource(R.string.field_alias_suffix))
                Text(aliasText)
            }
            if (canSelectSuffix) {
                ChevronDownIcon()
            }
        }
    }
}

class ThemeAliasOptionsPreviewProvider :
    ThemePairPreviewProvider<AliasAdvancedOptionsInput>(AliasAdvancedOptionsPreviewProvider())

@Preview
@Composable
fun AliasAdvancedOptionsSectionPreview(
    @PreviewParameter(ThemeAliasOptionsPreviewProvider::class)
    input: Pair<Boolean, AliasAdvancedOptionsInput>
) {

    PassTheme(isDark = input.first) {
        Surface {
            AliasAdvancedOptionsSection(
                enabled = true,
                isBottomSheet = false,
                prefix = input.second.prefix,
                suffix = input.second.suffix,
                isError = input.second.isError,
                canSelectSuffix = input.second.canSelectSuffix,
                onPrefixChanged = {},
                onSuffixClicked = {}
            )
        }
    }
}

@Preview
@Composable
@Suppress("FunctionMaxLength")
fun AliasAdvancedOptionsSectionBottomSheetPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class)
    input: Pair<Boolean, Boolean>
) {

    PassTheme(isDark = input.first) {
        Surface {
            AliasAdvancedOptionsSection(
                enabled = true,
                isBottomSheet = input.second,
                prefix = "prefix",
                suffix = AliasSuffixUiModel(
                    suffix = ".some@suffix.test",
                    signedSuffix = "",
                    isCustom = false,
                    domain = "suffix.test"
                ),
                isError = false,
                canSelectSuffix = true,
                onPrefixChanged = {},
                onSuffixClicked = {}
            )
        }
    }
}
