package proton.android.pass.ui

import me.proton.core.domain.entity.UserId

sealed interface AppNavigation {
    data class SignOut(val userId: UserId? = null) : AppNavigation
    object Report : AppNavigation
    object Subscription : AppNavigation
    object Upgrade : AppNavigation
    object Finish : AppNavigation
    object Restart : AppNavigation
}
