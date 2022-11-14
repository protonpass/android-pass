package me.proton.android.pass.navigation.api

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType

enum class NavArgId(val key: String, val navType: NavType<*>) {
    ItemId("itemId", NavType.StringType),
    ShareId("shareId", NavType.StringType),
}

inline fun <reified T> NavBackStackEntry.findArg(arg: NavArgId): T {
    val value = arguments?.get(arg.key)
    requireNotNull(value)
    return value as T
}
