package me.proton.pass.autofill.service.utils

import me.proton.pass.autofill.entities.AutofillFieldId
import kotlin.random.Random

fun newAutofillFieldId() = FakeAutofillFieldId()

/** Used for testing purposes */
class FakeAutofillFieldId(val id: Int = Random.nextInt()) : AutofillFieldId
