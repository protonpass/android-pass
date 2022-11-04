package me.proton.pass.data.extensions

import me.proton.pass.data.responses.AliasMailboxResponse
import me.proton.pass.data.responses.AliasOptionsResponse
import me.proton.pass.data.responses.AliasSuffixResponse
import me.proton.pass.domain.AliasMailbox
import me.proton.pass.domain.AliasOptions
import me.proton.pass.domain.AliasSuffix

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
