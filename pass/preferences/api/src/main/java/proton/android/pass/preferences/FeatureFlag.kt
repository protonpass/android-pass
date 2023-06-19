package proton.android.pass.preferences

enum class FeatureFlag(
    val title: String,
    val description: String,
    val key: String? = null
) {
    CREDIT_CARDS_ENABLED(
        "Credit cards",
        "Enable credit cards",
        // "PassCreditCardsV1" Temporarily deactivated until we allow FF by version or finish the feature
    )
}
