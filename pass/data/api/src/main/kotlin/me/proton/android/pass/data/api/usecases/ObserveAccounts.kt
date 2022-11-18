package me.proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.account.domain.entity.Account

interface ObserveAccounts {
    operator fun invoke(): Flow<List<Account>>
}
