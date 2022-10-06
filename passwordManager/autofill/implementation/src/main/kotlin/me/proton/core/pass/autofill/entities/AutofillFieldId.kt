package me.proton.core.pass.autofill.entities

import android.view.autofill.AutofillId

/** Used for testing purposes */
interface AutofillFieldId

/** Wrapper class holding an actual `AutofillId` */
data class AndroidAutofillFieldId(val autofillId: AutofillId) : AutofillFieldId

/**
 * Helper to do common casting to AndroidAutofillFieldId.
 *
 * **DO NOT** use this in unit tests.
 */
fun AutofillFieldId.asAndroid() = this as AndroidAutofillFieldId
