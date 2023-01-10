package proton.android.pass.autofill.entities

import androidx.compose.runtime.Immutable
import proton.android.pass.common.api.Option
import proton.pass.domain.entity.PackageName

@Immutable
data class AutofillAppState(
    val packageName: PackageName,
    val androidAutofillIds: List<AndroidAutofillFieldId>,
    val fieldTypes: List<FieldType>,
    val webDomain: Option<String>,
    val title: String
)

fun AutofillAppState.isEmpty(): Boolean =
    androidAutofillIds.isEmpty() || fieldTypes.isEmpty() || packageName.packageName.isEmpty()
