package proton.android.pass.featureitemcreate.impl.alias

import androidx.annotation.StringRes
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

enum class AliasSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {
    EmptyShareIdError(R.string.create_alias_empty_share_id, SnackbarType.ERROR),
    InitError(R.string.create_alias_init_error, SnackbarType.ERROR),
    ItemCreationError(R.string.create_alias_item_creation_error, SnackbarType.ERROR),
    CannotCreateMoreAliasesError(R.string.create_alias_cannot_create_more_aliases_error, SnackbarType.ERROR),
    ItemUpdateError(R.string.create_alias_item_update_error, SnackbarType.ERROR),
    AliasCreated(R.string.alias_created, SnackbarType.SUCCESS),
    AliasUpdated(R.string.changes_saved, SnackbarType.SUCCESS),
    AliasMovedToTrash(R.string.alias_moved_to_trash, SnackbarType.SUCCESS),
    CannotRetrieveAliasOptions(R.string.cannot_retrieve_alias_options, SnackbarType.ERROR)
}
