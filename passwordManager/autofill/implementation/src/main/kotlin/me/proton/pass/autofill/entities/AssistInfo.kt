package me.proton.pass.autofill.entities

data class AssistInfo(
    val fields: List<AssistField>,
    val url: String?
)
