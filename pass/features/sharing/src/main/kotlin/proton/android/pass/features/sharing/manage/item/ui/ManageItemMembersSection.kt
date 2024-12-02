/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.sharing.manage.item.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.shares.ShareMember
import proton.android.pass.features.sharing.R

@Composable
internal fun ManageItemMembersSection(modifier: Modifier = Modifier, members: List<ShareMember>) {
    Column(
        modifier = modifier
    ) {
        Text.Body2Medium(
            text = "${stringResource(R.string.sharing_member_count_header)} (${members.size})",
            color = PassTheme.colors.textWeak
        )

        members.forEach { member ->
            Text.Body2Medium(
                text = member.email
            )
        }
    }
}
