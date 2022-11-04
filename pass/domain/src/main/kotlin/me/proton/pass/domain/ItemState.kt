package me.proton.pass.domain

object ItemStateValues {
    const val ACTIVE = 1
    const val TRASHED = 2
}

enum class ItemState(val value: Int) {
    Active(ItemStateValues.ACTIVE),
    Trashed(ItemStateValues.TRASHED);

    companion object {
        val map = values().associateBy { it.value }
    }
}
