package me.proton.android.pass.data.impl.extensions

import me.proton.android.pass.data.api.PendingEventItemRevision
import me.proton.android.pass.data.impl.responses.ItemRevision

fun ItemRevision.toPendingEvent(): PendingEventItemRevision =
    PendingEventItemRevision(
        itemId = itemId,
        revision = revision,
        contentFormatVersion = contentFormatVersion,
        rotationId = rotationId,
        content = content,
        userSignature = userSignature,
        itemKeySignature = itemKeySignature,
        state = state,
        signatureEmail = signatureEmail,
        aliasEmail = aliasEmail,
        labels = labels,
        createTime = createTime,
        modifyTime = modifyTime
    )

fun PendingEventItemRevision.toItemRevision(): ItemRevision =
    ItemRevision(
        itemId = itemId,
        revision = revision,
        contentFormatVersion = contentFormatVersion,
        rotationId = rotationId,
        content = content,
        userSignature = userSignature,
        itemKeySignature = itemKeySignature,
        state = state,
        signatureEmail = signatureEmail,
        aliasEmail = aliasEmail,
        labels = labels,
        createTime = createTime,
        modifyTime = modifyTime
    )
