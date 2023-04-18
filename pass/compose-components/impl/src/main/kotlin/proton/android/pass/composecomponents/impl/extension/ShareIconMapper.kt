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

@Suppress("ComplexMethod")
@Composable
@DrawableRes
fun ShareIcon.toSmallResource(): Int = when (this) {
    ShareIcon.Icon1 -> R.drawable.ic_home_small
    ShareIcon.Icon2 -> R.drawable.ic_work_small
    ShareIcon.Icon3 -> R.drawable.ic_gift_small
    ShareIcon.Icon4 -> R.drawable.ic_shop_small
    ShareIcon.Icon5 -> R.drawable.ic_heart_small
    ShareIcon.Icon6 -> R.drawable.ic_bear_small
    ShareIcon.Icon7 -> R.drawable.ic_circles_small
    ShareIcon.Icon8 -> R.drawable.ic_flower_small
    ShareIcon.Icon9 -> R.drawable.ic_group_small
    ShareIcon.Icon10 -> R.drawable.ic_pacman_small
    ShareIcon.Icon11 -> R.drawable.ic_shopping_cart_small
    ShareIcon.Icon12 -> R.drawable.ic_leaf_small
    ShareIcon.Icon13 -> R.drawable.ic_shield_small
    ShareIcon.Icon14 -> R.drawable.ic_basketball_small
    ShareIcon.Icon15 -> R.drawable.ic_credit_card_small
    ShareIcon.Icon16 -> R.drawable.ic_fish_small
    ShareIcon.Icon17 -> R.drawable.ic_smile_small
    ShareIcon.Icon18 -> R.drawable.ic_lock_small
    ShareIcon.Icon19 -> R.drawable.ic_mushroom_small
    ShareIcon.Icon20 -> R.drawable.ic_star_small
    ShareIcon.Icon21 -> R.drawable.ic_fire_small
    ShareIcon.Icon22 -> R.drawable.ic_wallet_small
    ShareIcon.Icon23 -> R.drawable.ic_bookmark_small
    ShareIcon.Icon24 -> R.drawable.ic_cream_small
    ShareIcon.Icon25 -> R.drawable.ic_laptop_small
    ShareIcon.Icon26 -> R.drawable.ic_json_small
    ShareIcon.Icon27 -> R.drawable.ic_book_small
    ShareIcon.Icon28 -> R.drawable.ic_box_small
    ShareIcon.Icon29 -> R.drawable.ic_atom_small
    ShareIcon.Icon30 -> R.drawable.ic_cheque_small
}
