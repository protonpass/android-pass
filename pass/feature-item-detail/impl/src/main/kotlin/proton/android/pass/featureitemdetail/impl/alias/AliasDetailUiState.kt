package proton.android.pass.featureitemdetail.impl.alias

data class AliasDetailUiState(
    val isLoadingState: Boolean,
    val isItemSentToTrash: Boolean,
    val model: AliasUiModel?,
) {
    companion object {
        val Initial = AliasDetailUiState(
            isLoadingState = false,
            isItemSentToTrash = false,
            model = null,
        )
    }
}
