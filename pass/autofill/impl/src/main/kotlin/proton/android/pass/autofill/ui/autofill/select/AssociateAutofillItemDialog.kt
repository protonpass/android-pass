package proton.android.pass.autofill.ui.autofill.select

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.datetime.Clock
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import me.proton.core.compose.theme.headline
import proton.android.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

@Composable
internal fun AssociateAutofillItemDialog(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel?,
    onAssociateAndAutofill: (ItemUiModel) -> Unit,
    onAutofill: (ItemUiModel) -> Unit,
    onDismiss: () -> Unit
) {
    itemUiModel ?: return onDismiss()

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(backgroundColor = PassTheme.colors.backgroundNorm) {
            Column(
                modifier = modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.autofill_dialog_associate_title),
                    style = ProtonTheme.typography.headline
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = stringResource(
                        R.string.autofill_associate_web_app_name_dialog_title,
                        itemUiModel.name
                    ),
                    style = ProtonTheme.typography.default
                )
                DialogButton(
                    modifier = Modifier.align(Alignment.End),
                    text = stringResource(R.string.autofill_dialog_associate_and_autofill),
                    onClick = { onAssociateAndAutofill(itemUiModel) }
                )
                DialogButton(
                    modifier = Modifier.align(Alignment.End),
                    text = stringResource(R.string.autofill_dialog_just_autofill),
                    onClick = { onAutofill(itemUiModel) }
                )
                DialogButton(
                    modifier = Modifier.align(Alignment.End),
                    text = stringResource(R.string.autofill_dialog_cancel),
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
fun DialogButton(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    Button(
        modifier = modifier,
        elevation = null,
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        onClick = onClick
    ) {
        Text(
            text = text,
            style = ProtonTheme.typography.default,
            color = PassTheme.colors.interactionNormMajor2
        )
    }
}

@Preview
@Composable
fun AssociateAutofillItemDialogPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            AssociateAutofillItemDialog(
                itemUiModel = ItemUiModel(
                    id = ItemId(id = "ferri"),
                    shareId = ShareId(id = "rutrum"),
                    name = "Willie Lowe",
                    note = "repudiandae",
                    itemType = ItemType.Password,
                    state = 6128,
                    createTime = Clock.System.now(),
                    modificationTime = Clock.System.now(),
                    lastAutofillTime = null
                ),
                onAssociateAndAutofill = {},
                onAutofill = {},
                onDismiss = {}
            )
        }
    }
}
