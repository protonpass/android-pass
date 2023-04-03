package proton.android.pass.featurehome.impl.empty

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import me.proton.core.presentation.R as CoreR

@Composable
fun HomeEmptyList(
    modifier: Modifier = Modifier,
    onCreateLoginClick: () -> Unit,
    onCreateAliasClick: () -> Unit,
    onCreateNoteClick: () -> Unit,
    onCreatePasswordClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeEmptyHeader()
            Spacer(modifier = Modifier.height(16.dp))
            HomeEmptyButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = proton.android.pass.featurehome.impl.R.string.home_empty_vault_create_login),
                backgroundColor = PassTheme.colors.loginInteractionNormMinor2,
                textColor = PassTheme.colors.loginInteractionNormMajor1,
                icon = CoreR.drawable.ic_proton_user,
                onClick = onCreateLoginClick,
            )

            HomeEmptyButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = proton.android.pass.featurehome.impl.R.string.home_empty_vault_create_alias),
                backgroundColor = PassTheme.colors.aliasInteractionNormMinor2,
                textColor = PassTheme.colors.aliasInteractionNormMajor1,
                icon = CoreR.drawable.ic_proton_alias,
                onClick = onCreateAliasClick,
            )

            HomeEmptyButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = proton.android.pass.featurehome.impl.R.string.home_empty_vault_create_note),
                backgroundColor = PassTheme.colors.noteInteractionNormMinor2,
                textColor = PassTheme.colors.noteInteractionNormMajor1,
                icon = CoreR.drawable.ic_proton_notepad_checklist,
                onClick = onCreateNoteClick,
            )

            HomeEmptyButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(
                    id = proton.android.pass.featurehome.impl.R.string.home_empty_vault_create_password
                ),
                backgroundColor = PassTheme.colors.passwordInteractionNormMinor2,
                textColor = PassTheme.colors.passwordInteractionNormMajor1,
                icon = CoreR.drawable.ic_proton_key,
                onClick = onCreatePasswordClick,
            )
        }
    }
}

@Preview
@Composable
fun HomeEmptyListPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            HomeEmptyList(
                onCreateLoginClick = {},
                onCreateAliasClick = {},
                onCreateNoteClick = {},
                onCreatePasswordClick = {}
            )
        }
    }
}
