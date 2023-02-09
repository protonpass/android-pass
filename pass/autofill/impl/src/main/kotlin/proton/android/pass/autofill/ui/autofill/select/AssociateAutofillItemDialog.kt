package proton.android.pass.autofill.ui.autofill.select

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultStrong
import me.proton.pass.autofill.service.R
import proton.android.pass.commonuimodels.api.ItemUiModel

@Composable
internal fun AssociateAutofillItemDialog(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel?,
    packageName: String?,
    webDomain: String?,
    onAssociateAndAutofill: (ItemUiModel) -> Unit,
    onAutofill: (ItemUiModel) -> Unit,
    onDismiss: () -> Unit
) {
    itemUiModel ?: return onDismiss()

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card {
            Column(
                modifier = modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val associated = webDomain ?: packageName ?: ""
                Text(
                    text = stringResource(
                        R.string.autofill_associate_web_app_name_dialog_title,
                        associated,
                        itemUiModel.name
                    ),
                    style = ProtonTheme.typography.defaultStrong
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onAssociateAndAutofill(itemUiModel) }
                ) {
                    Text(text = stringResource(R.string.autofill_dialog_associate_and_autofill))
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onAutofill(itemUiModel) }
                ) {
                    Text(text = stringResource(R.string.autofill_dialog_just_autofill))
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss
                ) {
                    Text(text = stringResource(R.string.autofill_dialog_cancel))
                }
            }
        }
    }
}
