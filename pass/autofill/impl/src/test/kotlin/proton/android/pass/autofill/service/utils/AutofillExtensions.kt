package proton.android.pass.autofill.service.utils

import proton.android.pass.autofill.entities.AutofillFieldId
import kotlin.random.Random

fun newAutofillFieldId() = FakeAutofillFieldId()

/** Used for testing purposes */
class FakeAutofillFieldId(val id: Int = Random.nextInt()) : AutofillFieldId
