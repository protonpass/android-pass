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

package proton.android.pass.autofill.ui.autofill

import android.os.Bundle
import proton.android.pass.autofill.Utils
import proton.android.pass.autofill.entities.AutofillData
import proton.android.pass.autofill.entities.asAndroid
import proton.android.pass.autofill.extensions.marshalParcelable
import proton.android.pass.autofill.extensions.toAutofillItem
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.pass.domain.Item

object AutofillIntentExtras {
    const val ARG_AUTOFILL_IDS = "arg_autofill_ids"
    const val ARG_AUTOFILL_TYPES = "arg_autofill_types"
    const val ARG_AUTOFILL_IS_FOCUSED = "arg_autofill_is_focused"
    const val ARG_AUTOFILL_PARENT_ID = "arg_autofill_parent_id"
    const val ARG_PACKAGE_NAME = "arg_package_name"
    const val ARG_APP_NAME = "arg_app_name"
    const val ARG_WEB_DOMAIN = "arg_web_domain"
    const val ARG_TITLE = "arg_title"
    const val ARG_INLINE_SUGGESTION_AUTOFILL_ITEM = "arg_inline_suggestion_autofill_item"

    fun toExtras(data: AutofillData, itemOption: Option<Item> = None): Bundle {
        val extras = Bundle()
        if (data.assistInfo.url is Some) {
            extras.putString(ARG_WEB_DOMAIN, data.assistInfo.url.value)
        }
        val fields = data.assistInfo.fields
        val parentIdLists: List<AutofillIdList> = fields.map { field ->
            AutofillIdList(field.nodePath.map { it.asAndroid().autofillId })
        }

        val autofillIds = fields.map { it.id.asAndroid().autofillId }
        extras.putParcelableArrayList(ARG_AUTOFILL_IDS, autofillIds.toCollection(ArrayList()))

        extras.putStringArrayList(
            ARG_AUTOFILL_TYPES,
            fields.map { it.type?.toString() }.toCollection(ArrayList())
        )
        extras.putBooleanArray(
            ARG_AUTOFILL_IS_FOCUSED,
            fields.map { it.isFocused }.toBooleanArray()
        )

        val parentIds = AutofillIdListList(parentIdLists)
        extras.putByteArray(ARG_AUTOFILL_PARENT_ID, marshalParcelable(parentIds))
        extras.putString(
            ARG_PACKAGE_NAME,
            data.packageInfo.map { it.packageName.value }.value()
        )
        extras.putString(ARG_APP_NAME, data.packageInfo.map { it.appName.value }.value())
        extras.putString(
            ARG_TITLE,
            Utils.getTitle(data.assistInfo.url, data.packageInfo.map { it.appName.value })
        )
        if (itemOption is Some) {
            val autofillItem = itemOption.value.toAutofillItem()
            if (autofillItem is Some) {
                extras.putByteArray(
                    ARG_INLINE_SUGGESTION_AUTOFILL_ITEM,
                    marshalParcelable(autofillItem.value)
                )
            }
        }

        return extras
    }

}
