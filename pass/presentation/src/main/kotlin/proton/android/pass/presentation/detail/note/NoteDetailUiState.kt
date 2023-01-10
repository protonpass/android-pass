package proton.android.pass.presentation.detail.note

data class NoteDetailUiState(
    val title: String,
    val note: String
) {
    companion object {
        val Initial = NoteDetailUiState(
            title = "",
            note = ""
        )
    }
}
