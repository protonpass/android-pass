package proton.pass.domain

enum class ShareType(val value: Int) {
    Vault(1),
    Item(2);

    companion object {
        val map = values().associateBy { it.value }
    }
}
