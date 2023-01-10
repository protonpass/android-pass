package proton.android.pass.data.impl.extensions

import proton.android.pass.data.impl.responses.AliasMailboxResponse
import proton.android.pass.data.impl.responses.AliasOptionsResponse
import proton.android.pass.data.impl.responses.AliasSuffixResponse
import proton.pass.domain.AliasMailbox
import proton.pass.domain.AliasOptions
import proton.pass.domain.AliasSuffix

fun AliasOptionsResponse.toDomain(): AliasOptions =
    AliasOptions(
        suffixes = suffixes.map { it.toDomain() },
        mailboxes = mailboxes.map { it.toDomain() }
    )

fun AliasSuffixResponse.toDomain(): AliasSuffix =
    AliasSuffix(
        suffix = suffix,
        signedSuffix = signedSuffix,
        isCustom = isCustom,
        domain = domain
    )

fun AliasMailboxResponse.toDomain(): AliasMailbox =
    AliasMailbox(
        id = id,
        email = email
    )
