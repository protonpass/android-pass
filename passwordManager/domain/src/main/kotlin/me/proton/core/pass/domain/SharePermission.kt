package me.proton.core.pass.domain

import me.proton.core.util.kotlin.hasFlag

data class SharePermission(val value: Int)

fun SharePermission.hasFlag(flag: SharePermissionFlag): Boolean = value.hasFlag(flag.value)
fun SharePermission.flags(): List<SharePermissionFlag> =
    SharePermissionFlag.map.values.filter { hasFlag(it) }.toList()
