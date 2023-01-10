package proton.pass.domain

data class AliasSuffix(
    val suffix: String,
    val signedSuffix: String,
    val isCustom: Boolean,
    val domain: String
)
