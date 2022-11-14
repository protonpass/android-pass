package me.proton.android.pass.navigation.api

import androidx.navigation.NamedNavArgument

interface NavItem {
    val isTopLevel: Boolean
    val route: String
    val args: List<NamedNavArgument>
}
