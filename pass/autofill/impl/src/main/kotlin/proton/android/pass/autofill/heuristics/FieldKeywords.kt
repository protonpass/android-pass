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

package proton.android.pass.autofill.heuristics

import android.view.View
import proton.android.pass.autofill.entities.FieldType

private const val MOZILLA_BROWSER_PREFIX = "mozac"

internal data class FieldKeywords(
    val fieldType: FieldType,
    val allowedKeywords: List<String>,
    val deniedKeywords: List<String>
)

internal fun List<FieldKeywords>.match(field: String): FieldType = match(listOf(field)).first

internal fun List<FieldKeywords>.match(fields: List<String>): Pair<FieldType, String> {
    for ((fieldType, allowed, denied) in this) {
        for (value in fields) {
            for (keyword in allowed) {
                if (value.contains(keyword) && !denied.any { value.contains(it) }) {
                    return fieldType to value
                }
            }
        }
    }

    return FieldType.Unknown to ""
}

private val ALLOWED_ADDRESS_KEYWORDS = listOf(
    "address",
    "street",
    "calle",
    "casa",
    "home",
    "house"
)
private val ALLOWED_CITY_KEYWORDS = listOf("city")
private val ALLOWED_POSTAL_CODE_KEYWORDS = listOf("postal", "zip", "zipcode")
private val ALLOWED_PHONE_KEYWORDS = listOf("phone", "telef", "teleph", "mobile", "cellphone")
private val ALLOWED_EMAIL_KEYWORDS = listOf(View.AUTOFILL_HINT_EMAIL_ADDRESS, "email")
private val DEFAULT_DENIED_KEYWORDS = listOf("composer", "message", MOZILLA_BROWSER_PREFIX)

internal val fieldKeywordsList = listOf(
    kw(
        fieldType = FieldType.Username,
        allowedKeywords = listOf(
            View.AUTOFILL_HINT_USERNAME,
            "username",
            "identifier",
            "accountname",
            "userid"
        )
    ),
    kw(
        fieldType = FieldType.Email,
        allowedKeywords = ALLOWED_EMAIL_KEYWORDS
    ),
    kw(
        fieldType = FieldType.Password,
        allowedKeywords = listOf("password")
    ),
    kw(
        fieldType = FieldType.Totp,
        allowedKeywords = listOf("otp", "totp", "mfa", "2fa", "tfa")
    ),
    kw(
        fieldType = FieldType.CardNumber,
        allowedKeywords = listOf(
            "cardnumber",
            "cardnum",
            "ccnumber",
            "inputcard",
            "numerodetarjeta"
        )
    ),
    kw(
        fieldType = FieldType.CardCvv,
        allowedKeywords = listOf("cvc", "cvv", "securitycode")
    ),

    // Keywords for cardholder name are order-sensitve. First we want to test if we find
    // different fields for first name and last name, and if we can't, fallback to CardholderName
    kw(
        fieldType = FieldType.CardholderFirstName,
        allowedKeywords = listOf("firstname")
    ),
    kw(
        fieldType = FieldType.CardholderLastName,
        allowedKeywords = listOf("lastname")
    ),
    kw(
        fieldType = FieldType.FullName,
        allowedKeywords = listOf(
            "cardholder",
            "cardname",
            "holdername",
            "ccname",
            "namefull",
            "fullname"
        )
    ),

    // Keywords for expiration are order-sensitive. First we want to test for MMYY.
    // If we don't find it, we test for MM, and for the year, YYYY is more specific than YY,
    // so it needs to be evaluated first.
    kw(
        fieldType = FieldType.CardExpirationMMYY,
        allowedKeywords = listOf("mmyy", "mmaa")
    ),
    kw(
        fieldType = FieldType.CardExpirationMM,
        allowedKeywords = listOf(
            "cardmonth",
            "expmonth",
            "expirationmonth",
            "expirationdatemonth",
            "mesmm"
        )
    ),
    kw(
        fieldType = FieldType.CardExpirationYYYY,
        allowedKeywords = listOf("4digityear", "yyyy")
    ),
    kw(
        fieldType = FieldType.CardExpirationYY,
        allowedKeywords = listOf(
            "cardyear",
            "expyear",
            "expirationyear",
            "expirationdateyear",
            "yy",
            "anoaa"
        )
    ),
    kw(
        fieldType = FieldType.Address,
        allowedKeywords = ALLOWED_ADDRESS_KEYWORDS,
        deniedKeywords = ALLOWED_EMAIL_KEYWORDS +
            ALLOWED_POSTAL_CODE_KEYWORDS +
            ALLOWED_PHONE_KEYWORDS +
            ALLOWED_CITY_KEYWORDS +
            listOf("country", "button")
    ),
    kw(
        fieldType = FieldType.City,
        allowedKeywords = ALLOWED_CITY_KEYWORDS,
        deniedKeywords = ALLOWED_PHONE_KEYWORDS
    ),
    kw(
        fieldType = FieldType.PostalCode,
        allowedKeywords = ALLOWED_POSTAL_CODE_KEYWORDS
    ),
    kw(
        fieldType = FieldType.Phone,
        allowedKeywords = ALLOWED_PHONE_KEYWORDS
    )
)

private fun kw(
    fieldType: FieldType,
    allowedKeywords: List<String>,
    deniedKeywords: List<String> = emptyList()
) = FieldKeywords(fieldType, allowedKeywords, deniedKeywords + DEFAULT_DENIED_KEYWORDS)
