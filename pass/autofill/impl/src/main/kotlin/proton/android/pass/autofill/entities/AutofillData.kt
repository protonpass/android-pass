package proton.android.pass.autofill.entities

import proton.android.pass.common.api.Option

data class AutofillData(
    val assistInfo: AssistInfo,
    val packageName: Option<String>
)
