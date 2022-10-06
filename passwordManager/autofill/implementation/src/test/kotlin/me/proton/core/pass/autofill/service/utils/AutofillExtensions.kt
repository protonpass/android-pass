package me.proton.core.pass.autofill.service.utils

import me.proton.core.pass.autofill.entities.AutofillFieldId
import kotlin.random.Random

fun newAutofillFieldId() = FakeAutofillFieldId()

/** Used for testing purposes */
class FakeAutofillFieldId(val id: Int = Random.nextInt()) : AutofillFieldId
