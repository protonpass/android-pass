package me.proton.android.pass.data.impl.extensions

import me.proton.android.pass.data.impl.responses.ItemRevision
import me.proton.android.pass.data.api.ItemRevision as DomainItemRevision

fun ItemRevision.toDomain(): DomainItemRevision =
    DomainItemRevision(
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

fun DomainItemRevision.toData(): ItemRevision =
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
