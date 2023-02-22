package proton.android.pass.autofill.entities

import androidx.compose.runtime.Immutable
import proton.android.pass.common.api.Option
import proton.android.pass.commonuimodels.api.PackageInfoUi

@Immutable
data class AutofillAppState(
    val androidAutofillIds: List<AndroidAutofillFieldId>,
    val fieldTypes: List<FieldType>,
    val packageInfoUi: PackageInfoUi?,
    val webDomain: Option<String>,
    val title: String
)

fun AutofillAppState.isValid(): Boolean = androidAutofillIds.isEmpty() || fieldTypes.isEmpty()
