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
    val allowedKeywords: Set<String>,
    val deniedKeywords: Set<String>
)

internal fun Set<FieldKeywords>.match(vararg fields: String): Pair<FieldType, String> {
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

private val ALLOWED_ADDRESS_KEYWORDS = setOf(
    "address",
    "street",
    "direccion",
    "calle",
    "casa",
    "home",
    "house",
    "adresse"
)
private val DENIED_ADDRESS_KEYWORDS = setOf("street")
private val ALLOWED_CITY_KEYWORDS = setOf("city", "ciudad", "ville")
private val ALLOWED_COUNTRY_KEYWORDS = setOf("country", "pais")
private val ALLOWED_POSTAL_CODE_KEYWORDS = setOf("postal", "zip", "zipcode", "postcode")
private val ALLOWED_PHONE_KEYWORDS = setOf("phone", "telef", "teleph", "mobile", "cellphone")
private val ALLOWED_EMAIL_KEYWORDS = setOf(View.AUTOFILL_HINT_EMAIL_ADDRESS, "email")
private val DEFAULT_DENIED_KEYWORDS = setOf("composer", "message", MOZILLA_BROWSER_PREFIX)

internal val fieldKeywordsList = setOf(
    kw(
        fieldType = FieldType.Username,
        allowedKeywords = setOf(
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
        allowedKeywords = setOf("password")
    ),
    kw(
        fieldType = FieldType.Totp,
        allowedKeywords = setOf("otp", "totp", "mfa", "2fa", "tfa")
    ),
    kw(
        fieldType = FieldType.CardNumber,
        allowedKeywords = setOf(
            "cardnumber",
            "cardnum",
            "ccnumber",
            "inputcard",
            "numerodetarjeta"
        )
    ),
    kw(
        fieldType = FieldType.CardCvv,
        allowedKeywords = setOf("cvc", "cvv", "securitycode")
    ),
    kw(
        fieldType = FieldType.FullName,
        allowedKeywords = setOf(
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
        allowedKeywords = setOf("mmyy", "mmaa")
    ),
    kw(
        fieldType = FieldType.CardExpirationMM,
        allowedKeywords = setOf(
            "cardmonth",
            "expmonth",
            "expirationmonth",
            "expirationdatemonth",
            "mesmm"
        )
    ),
    kw(
        fieldType = FieldType.CardExpirationYYYY,
        allowedKeywords = setOf("4digityear", "yyyy")
    ),
    kw(
        fieldType = FieldType.CardExpirationYY,
        allowedKeywords = setOf(
            "cardyear",
            "expyear",
            "expirationyear",
            "expirationdateyear",
            "yy",
            "anoaa"
        )
    ),
    kw(
        fieldType = FieldType.City,
        allowedKeywords = ALLOWED_CITY_KEYWORDS,
        deniedKeywords = ALLOWED_PHONE_KEYWORDS + DENIED_ADDRESS_KEYWORDS
    ),
    kw(
        fieldType = FieldType.PostalCode,
        allowedKeywords = ALLOWED_POSTAL_CODE_KEYWORDS,
        deniedKeywords = setOf("address", "direccion", "adresse")
    ),
    kw(
        fieldType = FieldType.Address,
        allowedKeywords = ALLOWED_ADDRESS_KEYWORDS,
        deniedKeywords = ALLOWED_EMAIL_KEYWORDS +
            ALLOWED_PHONE_KEYWORDS +
            setOf("country", "button")
    ),
    kw(
        fieldType = FieldType.Phone,
        allowedKeywords = ALLOWED_PHONE_KEYWORDS
    ),
    kw(
        fieldType = FieldType.Country,
        allowedKeywords = ALLOWED_COUNTRY_KEYWORDS
    )
)

private fun kw(
    fieldType: FieldType,
    allowedKeywords: Set<String>,
    deniedKeywords: Set<String> = emptySet()
) = FieldKeywords(fieldType, allowedKeywords, deniedKeywords + DEFAULT_DENIED_KEYWORDS)
