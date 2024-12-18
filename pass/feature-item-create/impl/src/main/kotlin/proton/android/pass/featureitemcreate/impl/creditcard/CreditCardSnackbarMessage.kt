package proton.android.pass.featureitemcreate.impl.creditcard

import androidx.annotation.StringRes
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class CreditCardSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    ItemCreationError(R.string.create_credit_card_item_creation_error, SnackbarType.ERROR),
    ItemCreated(R.string.create_credit_card_item_creation_success, SnackbarType.SUCCESS),
    InitError(R.string.credit_card_init_error, SnackbarType.ERROR),
    AttachmentsInitError(R.string.update_credit_card_attachments_init_error, SnackbarType.ERROR),
    ItemUpdated(R.string.changes_saved, SnackbarType.SUCCESS),
    ItemUpdateError(R.string.credit_card_item_update_error, SnackbarType.ERROR),
    UpdateAppToUpdateItemError(R.string.snackbar_update_app_to_update_item, SnackbarType.ERROR)
}
