package me.proton.pass.autofill.entities

import me.proton.pass.common.api.Option

data class AssistInfo(
    val fields: List<AssistField>,
    val url: Option<String>
)
