/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.explore.telemetry

import proton.android.pass.protonapps.api.ProtonAppType
import proton.android.pass.telemetry.api.TelemetryEvent.DeferredTelemetryEvent

data object PassExploreDisplayHome : DeferredTelemetryEvent("pass_explore.display_home")

data class PassExploreToolClick(val tool: Tool) : DeferredTelemetryEvent("pass_explore.tool_click") {
    override fun dimensions(): Map<String, String> = mapOf("tool" to tool.telemetryValue)

    enum class Tool(val telemetryValue: String) {
        PasswordGenerator("password_generator"),
        DarkWebMonitor("dark_web_monitor"),
        PasswordHealth("password_health"),
        Codes("codes"),
        Aliases("aliases")
    }
}

data class PassExploreProductClick(
    val product: ProtonAppType,
    val action: Action
) : DeferredTelemetryEvent("pass_explore.product_click") {
    override fun dimensions(): Map<String, String> = mapOf(
        "product" to product.telemetryValue,
        "action" to action.telemetryValue
    )

    enum class Action(val telemetryValue: String) {
        OpenApp("open_app"),
        OpenStore("open_store"),
        OpenWeb("open_web")
    }
}

data object PassExploreBusinessBannerClick : DeferredTelemetryEvent("pass_explore.business_banner_click")

private val ProtonAppType.telemetryValue: String
    get() = when (this) {
        ProtonAppType.Vpn -> "vpn"
        ProtonAppType.Mail -> "mail"
        ProtonAppType.Drive -> "drive"
        ProtonAppType.Lumo -> "lumo"
        ProtonAppType.Meet -> "meet"
    }
