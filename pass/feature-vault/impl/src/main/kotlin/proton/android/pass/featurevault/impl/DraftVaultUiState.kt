package proton.android.pass.featurevault.impl

data class DraftVaultUiState(val title: String, val description: String) {
    fun validate(): Set<DraftVaultValidationErrors> {
        val mutableSet = mutableSetOf<DraftVaultValidationErrors>()
        if (title.isBlank()) mutableSet.add(DraftVaultValidationErrors.BlankTitle)
        return mutableSet.toSet()
    }
}


sealed interface DraftVaultValidationErrors {
    object BlankTitle : DraftVaultValidationErrors
}
