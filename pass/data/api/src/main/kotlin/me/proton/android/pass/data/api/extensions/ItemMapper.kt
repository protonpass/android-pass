package me.proton.android.pass.data.api.extensions

import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.toOption
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemType

fun Item.loginUsername(): Option<String> = when (val type = itemType) {
    is ItemType.Login -> type.username.toOption()
    else -> None
}
