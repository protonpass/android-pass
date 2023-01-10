package proton.pass.domain

enum class ShareType(val value: Int) {
    Vault(1),
    Label(2),
    Item(3);

    companion object {
        val map = values().associateBy { it.value }
    }
}
