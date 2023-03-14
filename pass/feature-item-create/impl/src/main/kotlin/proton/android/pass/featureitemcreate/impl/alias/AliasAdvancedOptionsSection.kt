package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featureitemcreate.impl.R

@Composable
fun AliasAdvancedOptionsSection(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    prefix: String,
    suffix: AliasSuffixUiModel?,
    isError: Boolean,
    canSelectSuffix: Boolean,
    onPrefixChanged: (String) -> Unit,
    onSuffixClicked: () -> Unit
) {
    RoundedCornersColumn(
        modifier = modifier
    ) {
        ProtonTextField(
            modifier = modifier.padding(16.dp),
            textStyle = ProtonTheme.typography.default,
            label = {
                ProtonTextFieldLabel(
                    text = stringResource(id = R.string.field_alias_prefix),
                    isError = isError
                )
            },
            value = prefix,
            onChange = onPrefixChanged,
            singleLine = true,
            moveToNextOnEnter = true,
            trailingIcon = if (prefix.isNotBlank() && enabled) {
                { SmallCrossIconButton { onPrefixChanged("") } }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        Divider()

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
                Icon(
                    painter = painterResource(id = me.proton.core.presentation.R.drawable.ic_proton_chevron_down),
                    contentDescription = null,
                )
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
