/*
 *  Copyright (c) 2021 Proton Technologies AG
 *  This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.pass.presentation.components.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.theme.ProtonDimens.DefaultCornerRadius
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonDimens.LargeSpacing
import me.proton.core.compose.theme.ProtonDimens.LargerSpacing
import me.proton.core.compose.theme.ProtonDimens.SmallSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.User
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.extensions.currentLocale

@Composable
fun UserSelector(
    currentUser: User,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val userSelectorContentDescription =
        stringResource(R.string.user_select_other_account_content_description)
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(DefaultCornerRadius))
            .background(ProtonTheme.colors.backgroundSecondary)
            .padding(SmallSpacing)
            .semantics { contentDescription = userSelectorContentDescription }
    ) {

        UserDetails(
            currentUser,
            modifier = Modifier.weight(1f)
        )

        Image(
            modifier = Modifier
                .size(LargeSpacing)
                .align(Alignment.CenterVertically),
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_chevron_down),
            colorFilter = ColorFilter.tint(Color.White),
            contentDescription = null
        )
    }
}

@Composable
fun UserDetails(
    user: User,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(LargerSpacing)
                .clip(RoundedCornerShape(DefaultCornerRadius))
                .background(ProtonTheme.colors.backgroundSecondary)
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = user.firstLetter.uppercase(LocalContext.current.currentLocale),
                style = ProtonTheme.typography.default,
                color = ProtonTheme.colors.textNorm
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = DefaultSpacing)
        ) {
            Text(
                text = user.displayName ?: "",
                style = ProtonTheme.typography.default,
                color = ProtonTheme.colors.textNorm,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = user.email ?: "",
                style = ProtonTheme.typography.defaultSmallWeak,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
@Composable
fun UserDetailsPreview() {
    ProtonTheme {
        Surface {
            UserDetails(PREVIEW_USER)
        }
    }
}

@Preview(widthDp = 250)
@Composable
fun UserDetailsWithLongNameAndAddressPreview() {
    ProtonTheme {
        Surface {
            UserDetails(
                PREVIEW_USER.copy(
                    displayName = "A very long name to see that everything holds on one line",
                    email = "a.very.long.name.to.see.that.everything.holds.on.one.line@protonmail.com"
                )
            )
        }
    }
}

@Preview
@Composable
fun UserSelectorPreview() {
    ProtonTheme(isDark = true) {
        Surface {
            UserSelector(
                currentUser = PREVIEW_USER
            ) {
            }
        }
    }
}

internal val User.firstLetter: Char
    get() = displayName?.firstOrNull() ?: name?.firstOrNull() ?: email?.firstOrNull() ?: '?'

internal val PREVIEW_USER = User(
    userId = UserId("id"),
    email = "adam@protonmail.com",
    name = "Adam Smith",
    displayName = "Adam Smith",
    currency = "€",
    credit = 0,
    usedSpace = 242_221_056L,
    maxSpace = 2_147_483_648L,
    maxUpload = 0,
    role = null,
    private = false,
    services = 0,
    subscribed = 0,
    delinquent = null,
    keys = emptyList()
)
