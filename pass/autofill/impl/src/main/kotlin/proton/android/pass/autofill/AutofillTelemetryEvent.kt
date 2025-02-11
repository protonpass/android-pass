/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.autofill

import proton.android.pass.autofill.extensions.isBrowser
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryEvent.DeferredTelemetryEvent

enum class AutofillTriggerSource(val source: String) {
    Source("source"),
    App("app")
}

data object AutosaveDone : DeferredTelemetryEvent("autosave.done")
data object AutosaveDisplay : DeferredTelemetryEvent("autosave.display")

data class AutofillDisplayed(
    val source: AutofillTriggerSource,
    val eventItemType: EventItemType,
    val app: PackageName
) : DeferredTelemetryEvent("autofill.display") {
    override fun dimensions(): Map<String, String> {
        val map = mutableMapOf("location" to source.source)
        if (app.isBrowser()) {
            map["mobileBrowser"] = app.value
        }
        map["itemType"] = eventItemType.itemTypeName
        return map
    }
}
data class AutofillDone(
    val source: AutofillTriggerSource,
    val eventItemType: EventItemType,
    val app: PackageName
) : DeferredTelemetryEvent("autofill.triggered") {
    override fun dimensions(): Map<String, String> {
        val map = mutableMapOf("location" to source.source)
        if (app.isBrowser()) {
            map["mobileBrowser"] = app.value
        }
        map["itemType"] = eventItemType.itemTypeName
        return map
    }
}

data object MFAAutofillCopied : DeferredTelemetryEvent("2fa.autofill")
