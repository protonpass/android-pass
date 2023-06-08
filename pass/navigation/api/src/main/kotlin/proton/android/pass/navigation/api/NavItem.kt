package proton.android.pass.navigation.api

import androidx.navigation.NamedNavArgument
import androidx.navigation.navArgument

open class NavItem(
    val baseRoute: String,
    private val navArgIds: List<NavArgId> = emptyList(),
    private val optionalArgIds: List<OptionalNavArgId> = emptyList(),
    val isTopLevel: Boolean = false,
    val isBottomsheet: Boolean = false,
) {
    val route: String = run {
        buildString {
            val argKeys = navArgIds.map { "{${it.key}}" }
            append(listOf(baseRoute).plus(argKeys).joinToString("/"))
            if (optionalArgIds.isNotEmpty()) {
                val optionalArgKeys = optionalArgIds.joinToString(
                    prefix = "?",
                    separator = "&",
                    transform = { "${it.key}={${it.key}}" }
                )
                append(optionalArgKeys)
            }
        }
    }

    val args: List<NamedNavArgument> =
        navArgIds.map { navArgument(it.key) { type = it.navType } }
            .plus(
                optionalArgIds.map {
                    navArgument(it.key) {
                        if (it.navType.isNullableAllowed) {
                            nullable = true
                        }
                        if (it.default != null) {
                            defaultValue = it.default
                        }
                        type = it.navType
                    }
                }
            )
}
