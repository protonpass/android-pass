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

package proton.android.pass.features.sl.sync.mailboxes.verify.navigation

import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.features.sl.sync.shared.navigation.mailboxes.SimpleLoginSyncMailboxIdNavArgId
import proton.android.pass.features.sl.sync.shared.navigation.mailboxes.SimpleLoginSyncPendingEmailNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.navigation.api.toPath

object SimpleLoginSyncMailboxVerifyNavItem : NavItem(
    baseRoute = "sl/sync/mailboxes/verify",
    navArgIds = listOf(SimpleLoginSyncMailboxIdNavArgId),
    optionalArgIds = listOf(SimpleLoginSyncPendingEmailNavArgId)
) {

    fun buildRoute(mailboxId: Long, pendingEmail: Option<String>): String = buildString {
        append("$baseRoute/$mailboxId")
        if (pendingEmail is Some) {
            val params = mapOf(SimpleLoginSyncPendingEmailNavArgId.key to NavParamEncoder.encode(pendingEmail.value))
            append(params.toPath())
        }
    }
}
