package me.proton.android.pass.ui.user

import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.SavedStateHandle
import me.proton.android.pass.extension.require
import me.proton.android.pass.ui.home.HomeScreen
import me.proton.core.domain.entity.UserId

/**
 * We need to do this because Hilt only support ViewModel which directly subclasses
 * [androidx.lifecycle.ViewModel]
 */
interface UserViewModel {

    val userId: UserId

    @ExperimentalMaterialApi
    companion object {
        operator fun invoke(
            savedStateHandle: SavedStateHandle,
            userIdKey: String = HomeScreen.userId,
        ) = object : UserViewModel {
            override val userId = UserId(savedStateHandle.require(userIdKey))
        }
    }
}
