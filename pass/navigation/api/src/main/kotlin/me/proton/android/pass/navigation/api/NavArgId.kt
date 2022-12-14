package me.proton.android.pass.navigation.api

import androidx.navigation.NavType

enum class NavArgId(val key: String, val navType: NavType<*>) {
    ItemId("itemId", NavType.StringType),
    ShareId("shareId", NavType.StringType),
}

sealed interface OptionalNavArgId {
    val key: String
    val navType: NavType<*>
}

enum class AliasOptionalNavArgId : OptionalNavArgId {
    Title {
        override val key: String = "aliasTitle"
        override val navType: NavType<*> = NavType.StringType
    }
}
