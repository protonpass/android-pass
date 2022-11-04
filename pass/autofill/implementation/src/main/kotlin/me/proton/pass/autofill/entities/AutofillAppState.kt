package me.proton.pass.autofill.entities

import androidx.compose.runtime.Immutable
import me.proton.pass.common.api.Option
import me.proton.pass.domain.entity.PackageName

@Immutable
data class AutofillAppState(
    val packageName: PackageName,
    val androidAutofillIds: List<AndroidAutofillFieldId>,
    val fieldTypes: List<FieldType>,
    val webDomain: Option<String>
)
