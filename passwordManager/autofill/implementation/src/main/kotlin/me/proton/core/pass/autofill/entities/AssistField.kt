package me.proton.core.pass.autofill.entities

import android.os.Parcelable
import android.view.autofill.AutofillValue
import kotlinx.parcelize.Parcelize

@Parcelize
data class AssistField(
    val id: AutofillFieldId,
    val type: FieldType?,
    val value: AutofillValue?,
    val text: String?
) : Parcelable
