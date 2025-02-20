/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.custom.shared

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import proton.android.pass.features.itemcreate.R
import me.proton.core.presentation.compose.R as CoreR

enum class TemplateType(
    val id: Int,
    val category: Category,
    @StringRes val titleResId: Int,
    @DrawableRes val iconResId: Int
) {
    API_CREDENTIAL(1, Category.TECHNOLOGY, R.string.template_item_api_credential, CoreR.drawable.ic_proton_code),
    DATABASE(2, Category.TECHNOLOGY, R.string.template_item_database, CoreR.drawable.ic_proton_storage),
    SERVER(3, Category.TECHNOLOGY, R.string.template_item_server, CoreR.drawable.ic_proton_servers),
    SOFTWARE_LICENSE(
        4,
        Category.TECHNOLOGY,
        R.string.template_item_software_license,
        CoreR.drawable.ic_proton_file_lines
    ),
    SSH_KEY(5, Category.TECHNOLOGY, R.string.template_item_ssh_key, CoreR.drawable.ic_proton_filing_cabinet),
    WIFI_NETWORK(6, Category.TECHNOLOGY, R.string.template_item_wifi_network, CoreR.drawable.ic_proton_shield_2_bolt),

    BANK_ACCOUNT(7, Category.FINANCE, R.string.template_item_bank_account, R.drawable.ic_bank),
    CRYPTO_WALLET(8, Category.FINANCE, R.string.template_item_crypto_wallet, R.drawable.ic_brand_bitcoin),

    DRIVER_LICENSE(9, Category.PERSONAL, R.string.template_item_driver_license, CoreR.drawable.ic_proton_card_identity),
    MEDICAL_RECORD(10, Category.PERSONAL, R.string.template_item_medical_record, CoreR.drawable.ic_proton_heart),
    MEMBERSHIP(11, Category.PERSONAL, R.string.template_item_membership, CoreR.drawable.ic_proton_user_circle),
    PASSPORT(12, Category.PERSONAL, R.string.template_item_passport, CoreR.drawable.ic_proton_card_identity),
    REWARD_PROGRAM(13, Category.PERSONAL, R.string.template_item_reward_program, CoreR.drawable.ic_proton_bag_percent),
    SOCIAL_SECURITY_NUMBER(
        14,
        Category.PERSONAL,
        R.string.template_item_social_security_number,
        CoreR.drawable.ic_proton_users
    );

    enum class Category {
        TECHNOLOGY,
        FINANCE,
        PERSONAL
    }

    companion object {
        fun fromId(id: Int): TemplateType? = entries.find { it.id == id }
    }
}
