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
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.features.itemcreate.R
import me.proton.core.presentation.compose.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

data class Field(@StringRes val nameResId: Int, val type: CustomFieldType)

enum class TemplateType(
    val id: Int,
    val category: Category,
    @StringRes val titleResId: Int,
    @DrawableRes val iconResId: Int,
    val fields: List<Field>
) {
    API_CREDENTIAL(
        id = 1,
        category = Category.TECHNOLOGY,
        titleResId = R.string.template_item_api_credential,
        iconResId = CoreR.drawable.ic_proton_code,
        fields = listOf(
            Field(R.string.template_api_credential_field_api_key, CustomFieldType.Hidden),
            Field(R.string.template_api_credential_field_secret, CustomFieldType.Hidden),
            Field(R.string.template_api_credential_field_expiry_date, CustomFieldType.Date),
            Field(R.string.template_api_credential_field_permissions, CustomFieldType.Text)
        )
    ),
    DATABASE(
        id = 2,
        category = Category.TECHNOLOGY,
        titleResId = R.string.template_item_database,
        iconResId = CoreR.drawable.ic_proton_storage,
        fields = listOf(
            Field(R.string.template_database_field_host, CustomFieldType.Text),
            Field(R.string.template_database_field_port, CustomFieldType.Text),
            Field(R.string.template_database_field_username, CustomFieldType.Text),
            Field(R.string.template_database_field_password, CustomFieldType.Hidden),
            Field(R.string.template_database_field_database_type, CustomFieldType.Text)
        )
    ),
    SERVER(
        id = 3,
        category = Category.TECHNOLOGY,
        titleResId = R.string.template_item_server,
        iconResId = CoreR.drawable.ic_proton_servers,
        fields = listOf(
            Field(R.string.template_server_field_ip_address, CustomFieldType.Text),
            Field(R.string.template_server_field_hostname, CustomFieldType.Text),
            Field(R.string.template_server_field_os, CustomFieldType.Text),
            Field(R.string.template_server_field_username, CustomFieldType.Text),
            Field(R.string.template_server_field_password, CustomFieldType.Hidden)
        )
    ),
    SOFTWARE_LICENSE(
        id = 4,
        category = Category.TECHNOLOGY,
        titleResId = R.string.template_item_software_license,
        iconResId = CoreR.drawable.ic_proton_file_lines,
        fields = listOf(
            Field(R.string.template_software_license_field_license_key, CustomFieldType.Hidden),
            Field(R.string.template_software_license_field_product, CustomFieldType.Text),
            Field(R.string.template_software_license_field_expiry_date, CustomFieldType.Date),
            Field(R.string.template_software_license_field_owner, CustomFieldType.Text)
        )
    ),
    SSH_KEY(
        id = 5,
        category = Category.TECHNOLOGY,
        titleResId = R.string.template_item_ssh_key,
        iconResId = CoreR.drawable.ic_proton_filing_cabinet,
        fields = listOf(
            Field(R.string.template_ssh_key_field_username, CustomFieldType.Text),
            Field(R.string.template_ssh_key_field_host, CustomFieldType.Text)
        )
    ),
    WIFI_NETWORK(
        id = 6,
        category = Category.TECHNOLOGY,
        titleResId = R.string.template_item_wifi_network,
        iconResId = CompR.drawable.ic_wifi,
        fields = listOf()
    ),
    BANK_ACCOUNT(
        id = 7,
        category = Category.FINANCE,
        titleResId = R.string.template_item_bank_account,
        iconResId = R.drawable.ic_bank,
        fields = listOf(
            Field(R.string.template_bank_account_field_bank_name, CustomFieldType.Text),
            Field(R.string.template_bank_account_field_account_number, CustomFieldType.Text),
            Field(R.string.template_bank_account_field_routing_number, CustomFieldType.Text),
            Field(R.string.template_bank_account_field_account_type, CustomFieldType.Text),
            Field(R.string.template_bank_account_field_iban, CustomFieldType.Hidden),
            Field(R.string.template_bank_account_field_swift_bic, CustomFieldType.Text),
            Field(R.string.template_bank_account_field_holder_name, CustomFieldType.Text)
        )
    ),
    CRYPTO_WALLET(
        id = 8,
        category = Category.FINANCE,
        titleResId = R.string.template_item_crypto_wallet,
        iconResId = R.drawable.ic_brand_bitcoin,
        fields = listOf(
            Field(R.string.template_crypto_wallet_field_wallet_name, CustomFieldType.Text),
            Field(R.string.template_crypto_wallet_field_address, CustomFieldType.Text),
            Field(R.string.template_crypto_wallet_field_private_key, CustomFieldType.Hidden),
            Field(R.string.template_crypto_wallet_field_seed_phrase, CustomFieldType.Hidden),
            Field(R.string.template_crypto_wallet_field_network, CustomFieldType.Text)
        )
    ),
    DRIVER_LICENSE(
        id = 9,
        category = Category.PERSONAL,
        titleResId = R.string.template_item_driver_license,
        iconResId = CoreR.drawable.ic_proton_card_identity,
        fields = listOf(
            Field(R.string.template_driver_license_field_full_name, CustomFieldType.Text),
            Field(R.string.template_driver_license_field_license_number, CustomFieldType.Text),
            Field(R.string.template_driver_license_field_issuing_state_country, CustomFieldType.Text),
            Field(R.string.template_driver_license_field_expiry_date, CustomFieldType.Date),
            Field(R.string.template_driver_license_field_date_of_birth, CustomFieldType.Date),
            Field(R.string.template_driver_license_field_class, CustomFieldType.Text)
        )
    ),
    MEDICAL_RECORD(
        id = 10,
        category = Category.PERSONAL,
        titleResId = R.string.template_item_medical_record,
        iconResId = CoreR.drawable.ic_proton_heart,
        fields = listOf(
            Field(R.string.template_medical_record_field_patient_name, CustomFieldType.Text),
            Field(R.string.template_medical_record_field_record_number, CustomFieldType.Hidden),
            Field(R.string.template_medical_record_field_medical_conditions, CustomFieldType.Hidden),
            Field(R.string.template_medical_record_field_medications, CustomFieldType.Hidden),
            Field(R.string.template_medical_record_field_doctor, CustomFieldType.Text),
            Field(R.string.template_medical_record_field_hospital, CustomFieldType.Text)
        )
    ),
    MEMBERSHIP(
        id = 11,
        category = Category.PERSONAL,
        titleResId = R.string.template_item_membership,
        iconResId = CoreR.drawable.ic_proton_user_circle,
        fields = listOf(
            Field(R.string.template_membership_field_organization_name, CustomFieldType.Text),
            Field(R.string.template_membership_field_membership_id, CustomFieldType.Text),
            Field(R.string.template_membership_field_member_name, CustomFieldType.Text),
            Field(R.string.template_membership_field_expiry_date, CustomFieldType.Date),
            Field(R.string.template_membership_field_tier_level, CustomFieldType.Text)
        )
    ),
    PASSPORT(
        id = 12,
        category = Category.PERSONAL,
        titleResId = R.string.template_item_passport,
        iconResId = CoreR.drawable.ic_proton_card_identity,
        fields = listOf(
            Field(R.string.template_passport_field_full_name, CustomFieldType.Text),
            Field(R.string.template_passport_field_passport_number, CustomFieldType.Hidden),
            Field(R.string.template_passport_field_country, CustomFieldType.Text),
            Field(R.string.template_passport_field_expiry_date, CustomFieldType.Date),
            Field(R.string.template_passport_field_date_of_birth, CustomFieldType.Date),
            Field(R.string.template_passport_field_issuing_authority, CustomFieldType.Text)
        )
    ),
    REWARD_PROGRAM(
        id = 13,
        category = Category.PERSONAL,
        titleResId = R.string.template_item_reward_program,
        iconResId = CoreR.drawable.ic_proton_bag_percent,
        fields = listOf(
            Field(R.string.template_reward_program_field_program_name, CustomFieldType.Text),
            Field(R.string.template_reward_program_field_member_id, CustomFieldType.Text),
            Field(R.string.template_reward_program_field_points_balance, CustomFieldType.Text),
            Field(R.string.template_reward_program_field_expiry_date, CustomFieldType.Date),
            Field(R.string.template_reward_program_field_tier_status, CustomFieldType.Text)
        )
    ),
    SOCIAL_SECURITY_NUMBER(
        id = 14,
        category = Category.PERSONAL,
        titleResId = R.string.template_item_social_security_number,
        iconResId = CoreR.drawable.ic_proton_users,
        fields = listOf(
            Field(R.string.template_social_security_number_field_full_name, CustomFieldType.Text),
            Field(R.string.template_social_security_number_field_ssn, CustomFieldType.Hidden),
            Field(R.string.template_social_security_number_field_issuing_country, CustomFieldType.Text)
        )
    );

    enum class Category {
        TECHNOLOGY,
        FINANCE,
        PERSONAL
    }

    companion object {
        fun fromId(id: Int): TemplateType = entries.find { it.id == id }
            ?: throw IllegalStateException("Unknown template type id: $id")
    }
}
