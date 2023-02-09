package proton.android.pass.autofill.entities

import androidx.compose.runtime.Immutable
import proton.android.pass.common.api.Option
import proton.pass.domain.entity.PackageName

@Immutable
data class AutofillAppState(
    val androidAutofillIds: List<AndroidAutofillFieldId>,
    val fieldTypes: List<FieldType>,
    val packageName: Option<PackageName>,
    val webDomain: Option<String>,
    val title: String
)

fun AutofillAppState.isValid(): Boolean = androidAutofillIds.isEmpty() || fieldTypes.isEmpty()
