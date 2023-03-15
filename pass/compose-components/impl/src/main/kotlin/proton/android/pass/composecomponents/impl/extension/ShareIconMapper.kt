package proton.android.pass.composecomponents.impl.extension

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import proton.android.pass.composecomponents.impl.R
import proton.pass.domain.ShareIcon

@Suppress("ComplexMethod")
@Composable
@DrawableRes
fun ShareIcon.toResource(): Int = when (this) {
    ShareIcon.Icon1 -> R.drawable.ic_house
    ShareIcon.Icon2 -> R.drawable.ic_cheque
    ShareIcon.Icon3 -> R.drawable.ic_shop
    ShareIcon.Icon4 -> R.drawable.ic_palm_tree
    ShareIcon.Icon5 -> R.drawable.ic_savings
    ShareIcon.Icon6 -> R.drawable.ic_discount
    ShareIcon.Icon7 -> R.drawable.ic_run_shoes
    ShareIcon.Icon8 -> R.drawable.ic_chef
    ShareIcon.Icon9 -> R.drawable.ic_shopping_bag
    ShareIcon.Icon10 -> R.drawable.ic_mario_mushroom
    ShareIcon.Icon11 -> R.drawable.ic_wallet
    ShareIcon.Icon12 -> R.drawable.ic_hacker
    ShareIcon.Icon13 -> R.drawable.ic_present
    ShareIcon.Icon14 -> R.drawable.ic_medal
    ShareIcon.Icon15 -> R.drawable.ic_teddy_bear
    ShareIcon.Icon16 -> R.drawable.ic_pacman
    ShareIcon.Icon17 -> R.drawable.ic_shield
    ShareIcon.Icon18 -> R.drawable.ic_book_bookmark
    ShareIcon.Icon19 -> R.drawable.ic_witch_hat
    ShareIcon.Icon20 -> R.drawable.ic_atom
    ShareIcon.Icon21 -> R.drawable.ic_briefcase
    ShareIcon.Icon22 -> R.drawable.ic_love
    ShareIcon.Icon23 -> R.drawable.ic_chemistry
    ShareIcon.Icon24 -> R.drawable.ic_grain
    ShareIcon.Icon25 -> R.drawable.ic_credit_card
    ShareIcon.Icon26 -> R.drawable.ic_router
    ShareIcon.Icon27 -> R.drawable.ic_volleyball
    ShareIcon.Icon28 -> R.drawable.ic_happy_baby
    ShareIcon.Icon29 -> R.drawable.ic_alien
    ShareIcon.Icon30 -> R.drawable.ic_car
}
