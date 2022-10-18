package me.proton.core.pass.presentation.create.alias

import androidx.annotation.StringRes
import me.proton.core.pass.presentation.R

enum class AliasSnackbarMessage(@StringRes val id: Int) {
    EmptyShareIdError(R.string.create_alias_empty_share_id),
    InitError(R.string.create_alias_init_error),
    ItemCreationError(R.string.create_alias_item_creation_error),
    ItemUpdateError(R.string.create_alias_item_update_error)
}
