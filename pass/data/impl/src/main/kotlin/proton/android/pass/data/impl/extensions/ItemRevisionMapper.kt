package proton.android.pass.data.impl.extensions

import proton.android.pass.data.api.PendingEventItemRevision
import proton.android.pass.data.impl.responses.ItemRevision

fun ItemRevision.toPendingEvent(): PendingEventItemRevision =
    PendingEventItemRevision(
        itemId = itemId,
        revision = revision,
        contentFormatVersion = contentFormatVersion,
        keyRotation = keyRotation,
        content = content,
        state = state,
        aliasEmail = aliasEmail,
        createTime = createTime,
        modifyTime = modifyTime,
        lastUseTime = lastUseTime,
        revisionTime = revisionTime,
        key = itemKey,
    )

fun PendingEventItemRevision.toItemRevision(): ItemRevision =
    ItemRevision(
        itemId = itemId,
        revision = revision,
        contentFormatVersion = contentFormatVersion,
        keyRotation = keyRotation,
        content = content,
        state = state,
        aliasEmail = aliasEmail,
        createTime = createTime,
        modifyTime = modifyTime,
        lastUseTime = lastUseTime,
        revisionTime = revisionTime,
        itemKey = key,
    )
