package proton.android.pass.featureitemdetail.impl.note

data class NoteDetailUiState(
    val title: String,
    val note: String,
    val isLoading: Boolean,
    val isItemSentToTrash: Boolean,
) {
    companion object {
        val Initial = NoteDetailUiState(
            title = "",
            note = "",
            isLoading = false,
            isItemSentToTrash = false
        )
    }
}
