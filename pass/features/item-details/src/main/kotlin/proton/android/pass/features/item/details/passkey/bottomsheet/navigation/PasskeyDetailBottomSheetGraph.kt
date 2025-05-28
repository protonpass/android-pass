/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.features.item.details.passkey.bottomsheet.navigation

import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.toPath

object PasskeyIdNavArgId : OptionalNavArgId {
    override val key: String = "passkeyId"
    override val navType: NavType<*> = NavType.StringType
}

internal object ViewPasskeyDetailsModeNavArgId : NavArgId {
    override val key: String = "mode"
    override val navType: NavType<*> = NavType.StringType
}

internal object DirectPasskeyNavArgId : OptionalNavArgId {
    override val key: String = "passkey"
    override val navType: NavType<*> = NavType.StringType

    fun create(passkey: UIPasskeyContent) = NavParamEncoder.encode(
        value = Json.encodeToString(value = passkey)
    )

    fun decode(encoded: String): UIPasskeyContent = Json.decodeFromString(
        string = NavParamEncoder.decode(encoded)
    )

}

enum class ViewPasskeyDetailsMode {
    References,
    Direct
}

object ViewPasskeyDetailsBottomSheet : NavItem(
    baseRoute = "item/detail/login/passkey/bottomsheet",
    navArgIds = listOf(ViewPasskeyDetailsModeNavArgId),
    optionalArgIds = listOf(
        CommonOptionalNavArgId.ShareId,
        CommonOptionalNavArgId.ItemId,
        PasskeyIdNavArgId,
        DirectPasskeyNavArgId
    )
) {
    fun buildRoute(
        shareId: ShareId,
        itemId: ItemId,
        passkeyId: PasskeyId
    ) = buildString {
        append("$baseRoute/${ViewPasskeyDetailsMode.References.name}")

        val params = mapOf(
            CommonOptionalNavArgId.ShareId.key to shareId.id,
            CommonOptionalNavArgId.ItemId.key to itemId.id,
            PasskeyIdNavArgId.key to passkeyId.value
        )

        append(params.toPath())
    }

    fun buildRoute(passkey: UIPasskeyContent) = buildString {
        append("$baseRoute/${ViewPasskeyDetailsMode.Direct.name}")

        val params = mapOf(
            DirectPasskeyNavArgId.key to DirectPasskeyNavArgId.create(passkey)
        )

        append(params.toPath())
    }
}
