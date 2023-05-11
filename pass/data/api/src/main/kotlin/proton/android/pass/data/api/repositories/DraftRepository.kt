package proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.Option

const val DRAFT_PASSWORD_KEY = "draftpassword"

interface DraftRepository {
    fun save(key: String, value: Any)
    fun <T> get(key: String): Flow<Option<T>>
    fun <T> delete(key: String): Option<T>
}
