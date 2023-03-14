package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.alias.mailboxes.AliasToBeCreatedInput
import proton.android.pass.featureitemcreate.impl.alias.mailboxes.AliasToBeCreatedPreviewProvider

@Composable
fun AliasToBeCreated(
    modifier: Modifier = Modifier,
    prefix: String,
    suffix: AliasSuffixUiModel?
) {
    Row(
        modifier = modifier
            .roundedContainer(ProtonTheme.colors.separatorNorm)
            .background(PassTheme.colors.backgroundNorm)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_alias),
            contentDescription = null,
            tint = PassTheme.colors.accentGreenNorm
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            ProtonTextFieldLabel(text = stringResource(id = R.string.field_alias_you_are_about_to_create))
            AliasPrefixSuffixText(
                prefix = prefix,
                suffix = suffix?.suffix ?: "",
                suffixColor = PassTheme.colors.accentGreenNorm
            )
        }
    }
}

class ThemedAliasToBeCreatedPreviewProvider :
    ThemePairPreviewProvider<AliasToBeCreatedInput>(AliasToBeCreatedPreviewProvider())

@Preview
@Composable
fun AliasToBeCreatedPreview(
    @PreviewParameter(ThemedAliasToBeCreatedPreviewProvider::class) input: Pair<Boolean, AliasToBeCreatedInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AliasToBeCreated(
                prefix = input.second.prefix,
                suffix = input.second.suffix
            )
        }
    }
}
