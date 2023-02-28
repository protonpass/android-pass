package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.pass.domain.Item

interface ObserveTrashedItems {
    operator fun invoke(): Flow<List<Item>>
}
