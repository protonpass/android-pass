package proton.android.pass.navigation.api

import androidx.navigation.NavType

sealed interface NavArgId {
    val key: String
    val navType: NavType<*>
}

private const val SHARE_ID_KEY = "shareId"

enum class CommonNavArgId : NavArgId {
    ItemId {
        override val key: String = "itemId"
        override val navType: NavType<*> = NavType.StringType
    },
    ShareId {
        override val key: String = SHARE_ID_KEY
        override val navType: NavType<*> = NavType.StringType
    }
}

sealed interface OptionalNavArgId : NavArgId {
    val default: Any?
        get() = null
}

enum class CommonOptionalNavArgId : OptionalNavArgId {
    ShareId {
        override val key: String = SHARE_ID_KEY
        override val navType: NavType<*> = NavType.StringType
    }
}

enum class AliasOptionalNavArgId : OptionalNavArgId {
    Title {
        override val key: String = "aliasTitle"
        override val navType: NavType<*> = NavType.StringType
    },
    IsDraft {
        override val key: String = "isDraft"
        override val navType: NavType<*> = NavType.BoolType
        override val default: Any
            get() = false
    }
}
