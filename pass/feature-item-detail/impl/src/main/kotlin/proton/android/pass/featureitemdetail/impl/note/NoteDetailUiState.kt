package proton.android.pass.featureitemdetail.impl.note

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
