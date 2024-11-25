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
import proton.android.pass.autofill.heuristics.Language.English
import proton.android.pass.autofill.heuristics.Language.French
import proton.android.pass.autofill.heuristics.Language.Spanish

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

enum class Language {
    English,
    Spanish,
    French
}

private val ALLOWED_ADDRESS_KEYWORDS = mapOf(
    English to setOf("address", "street", "home", "house"),
    Spanish to setOf("direccion", "calle", "casa"),
    French to setOf("adresse")
)
private val DENIED_ADDRESS_KEYWORDS = mapOf(
    English to setOf("line2"),
    Spanish to setOf("linea2"),
    French to setOf("ligne2")
)

private val DENIED_CITY_KEYWORDS = mapOf(
    English to setOf("street"),
    Spanish to setOf("calle"),
    French to setOf("rue")
)

private val ALLOWED_CITY_KEYWORDS = mapOf(
    English to setOf("city"),
    Spanish to setOf("ciudad"),
    French to setOf("ville")
)

private val ALLOWED_COUNTRY_KEYWORDS = mapOf(
    English to setOf("country"),
    Spanish to setOf("pais"),
    French to setOf("pays")
)
private val ALLOWED_POSTAL_CODE_KEYWORDS = mapOf(
    English to setOf("postal", "zip", "zipcode", "postcode")
)
private val ALLOWED_PHONE_KEYWORDS = mapOf(
    English to setOf("phone", "teleph", "mobile", "cellphone"),
    Spanish to setOf("telef", "movil", "celular")
)
private val ALLOWED_EMAIL_KEYWORDS = setOf("email")
private val ALLOWED_ORGANIZATION_KEYWORDS = mapOf(
    English to setOf("organi", "company", "business")
)
private val ALLOWED_FIRST_NAME_KEYWORDS = mapOf(
    English to setOf("firstname", "givenname")
)
private val ALLOWED_MIDDLE_NAME_KEYWORDS = mapOf(
    English to setOf("middlename", "additionalname")
)
private val ALLOWED_LAST_NAME_KEYWORDS = mapOf(
    English to setOf("lastname", "familyname")
)
private val DEFAULT_DENIED_KEYWORDS = setOf(
    "composer",
    "message",
    MOZILLA_BROWSER_PREFIX
)
internal val DENIED_USERNAME_KEYWORDS = setOf(
    "connectionsroletag" // discord specific
)

internal val fieldKeywordsList = setOf(
    kw(
        fieldType = FieldType.Username,
        allowedKeywords = setOf(
            View.AUTOFILL_HINT_USERNAME,
            "username",
            "identifier",
            "accountname",
            "userid"
        ),
        deniedKeywords = DENIED_USERNAME_KEYWORDS
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
        fieldType = FieldType.FirstName,
        allowedKeywords = ALLOWED_FIRST_NAME_KEYWORDS.flattenedValues()
    ),
    kw(
        fieldType = FieldType.MiddleName,
        allowedKeywords = ALLOWED_MIDDLE_NAME_KEYWORDS.flattenedValues()
    ),
    kw(
        fieldType = FieldType.LastName,
        allowedKeywords = ALLOWED_LAST_NAME_KEYWORDS.flattenedValues()
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
        allowedKeywords = ALLOWED_CITY_KEYWORDS.values.flatten().toSet(),
        deniedKeywords = ALLOWED_PHONE_KEYWORDS.flattenedValues() + DENIED_CITY_KEYWORDS.flattenedValues()
    ),
    kw(
        fieldType = FieldType.PostalCode,
        allowedKeywords = ALLOWED_POSTAL_CODE_KEYWORDS.flattenedValues(),
        deniedKeywords = setOf("address", "direccion", "adresse")
    ),
    kw(
        fieldType = FieldType.Address,
        allowedKeywords = ALLOWED_ADDRESS_KEYWORDS.flattenedValues(),
        deniedKeywords = DENIED_ADDRESS_KEYWORDS.flattenedValues() +
            ALLOWED_EMAIL_KEYWORDS +
            ALLOWED_PHONE_KEYWORDS.flattenedValues() +
            ALLOWED_COUNTRY_KEYWORDS.flattenedValues() +
            setOf("button")
    ),
    kw(
        fieldType = FieldType.Phone,
        allowedKeywords = ALLOWED_PHONE_KEYWORDS.flattenedValues()
    ),
    kw(
        fieldType = FieldType.Country,
        allowedKeywords = ALLOWED_COUNTRY_KEYWORDS.flattenedValues()
    ),
    kw(
        fieldType = FieldType.Organization,
        allowedKeywords = ALLOWED_ORGANIZATION_KEYWORDS.flattenedValues()
    )
)

private fun Map<Language, Set<String>>.flattenedValues() = values.flatten().toSet()

private fun kw(
    fieldType: FieldType,
    allowedKeywords: Set<String>,
    deniedKeywords: Set<String> = emptySet()
) = FieldKeywords(fieldType, allowedKeywords, deniedKeywords + DEFAULT_DENIED_KEYWORDS)
