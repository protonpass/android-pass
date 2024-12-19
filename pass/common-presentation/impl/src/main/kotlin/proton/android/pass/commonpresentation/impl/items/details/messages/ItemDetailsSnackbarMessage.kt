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

package proton.android.pass.commonpresentation.impl.items.details.messages

import androidx.annotation.StringRes
import proton.android.pass.commonpresentation.impl.R
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType

internal enum class ItemDetailsSnackbarMessage(
    @StringRes override val id: Int,
    override val type: SnackbarType,
    override val isClipboard: Boolean = false
) : SnackbarMessage {

    AliasCopied(
        id = R.string.item_details_snackbar_message_alias_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    BirthDateCopied(
        id = R.string.item_details_snackbar_message_birthdate_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    CardNumberCopied(
        id = R.string.item_details_snackbar_message_card_number_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    CityCopied(
        id = R.string.item_details_snackbar_message_city_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    CompanyCopied(
        id = R.string.item_details_snackbar_message_company_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    CountryOrRegionCopied(
        id = R.string.item_details_snackbar_message_country_or_region_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    CountyCopied(
        id = R.string.item_details_snackbar_message_county_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    CustomFieldCopied(
        id = R.string.item_details_snackbar_message_custom_field_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    CvvCopied(
        id = R.string.item_details_snackbar_message_cvv_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    EmailCopied(
        id = R.string.item_details_snackbar_message_email_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    FacebookCopied(
        id = R.string.item_details_snackbar_message_facebook_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    FirstNameCopied(
        id = R.string.item_details_snackbar_message_first_name_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    FloorCopied(
        id = R.string.item_details_snackbar_message_floor_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    FullNameCopied(
        id = R.string.item_details_snackbar_message_full_name_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    GenderCopied(
        id = R.string.item_details_snackbar_message_gender_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    InstagramCopied(
        id = R.string.item_details_snackbar_message_instagram_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    LastNameCopied(
        id = R.string.item_details_snackbar_message_last_name_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    LicenseNumberCopied(
        id = R.string.item_details_snackbar_message_license_number_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    LinkedInCopied(
        id = R.string.item_details_snackbar_message_linkedin_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    MiddleNameCopied(
        id = R.string.item_details_snackbar_message_middle_name_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    OccupationCopied(
        id = R.string.item_details_snackbar_message_occupation_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    OrganizationCopied(
        id = R.string.item_details_snackbar_message_organization_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    PassportNumberCopied(
        id = R.string.item_details_snackbar_message_passport_number_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    PhoneNumberCopied(
        id = R.string.item_details_snackbar_message_phone_number_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    RedditCopied(
        id = R.string.item_details_snackbar_message_reddit_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    SocialSecurityNumberCopied(
        id = R.string.item_details_snackbar_message_social_security_number_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    StateOrProvinceCopied(
        id = R.string.item_details_snackbar_message_state_or_province_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    StreetAddressCopied(
        id = R.string.item_details_snackbar_message_street_address_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    PasswordCopied(
        id = R.string.item_details_snackbar_message_password_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    PinCopied(
        id = R.string.item_details_snackbar_message_pin_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    TotpCodeCopied(
        id = R.string.item_details_snackbar_message_totp_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    UsernameCopied(
        id = R.string.item_details_snackbar_message_username_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    WebsiteCopied(
        id = R.string.item_details_snackbar_message_website_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    XHandleCopied(
        id = R.string.item_details_snackbar_message_x_handle_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    YahooCopied(
        id = R.string.item_details_snackbar_message_yahoo_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    ZipOrPostalCodeCopied(
        id = R.string.item_details_snackbar_message_zip_or_postal_code_copied,
        type = SnackbarType.NORM,
        isClipboard = true
    ),

    OpenAttachmentsError(R.string.open_attachments_error, SnackbarType.ERROR)

}
