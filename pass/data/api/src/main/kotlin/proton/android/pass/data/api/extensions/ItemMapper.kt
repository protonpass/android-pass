package proton.android.pass.data.api.extensions

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.pass.domain.Item
import proton.pass.domain.ItemType

fun Item.loginUsername(): Option<String> = when (val type = itemType) {
    is ItemType.Login -> type.username.toOption()
    else -> None
}
