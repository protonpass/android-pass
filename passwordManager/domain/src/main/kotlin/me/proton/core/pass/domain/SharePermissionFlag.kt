package me.proton.core.pass.domain

enum class SharePermissionFlag(val value: Int) {
    Admin(1 shl 0),
    Read(1 shl 1),
    Create(1 shl 2),
    Update(1 shl 3),
    Trash(1 shl 4),
    Delete(1 shl 5),
    CreateLabel(1 shl 6),
    TrashLabel(1 shl 7),
    AttachLabel(1 shl 8),
    DetachLabel(1 shl 9);

    companion object {
        val map = values().associateBy { it.value }
    }
}
