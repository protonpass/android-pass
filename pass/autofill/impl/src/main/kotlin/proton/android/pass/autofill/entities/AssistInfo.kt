package proton.android.pass.autofill.entities

import proton.android.pass.common.api.Option

data class AssistInfo(
    val fields: List<AssistField>,
    val url: Option<String>
)
