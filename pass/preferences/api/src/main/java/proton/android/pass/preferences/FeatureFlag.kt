package proton.android.pass.preferences

enum class FeatureFlag(
    val title: String,
    val description: String,
    val key: String? = null
) {
    CUSTOM_FIELDS_ENABLED(
        "Custom fields",
        "Enable custom fields",
        "PassCustomFields"
    ),
    CREDIT_CARDS_ENABLED(
        "Credit cards",
        "Enable credit cards",
        "PassCreditCardsV1"
    )
}
