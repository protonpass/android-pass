package proton.android.pass.passkeys.fakes

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.passkeys.api.AuthenticateWithPasskey
import proton.android.pass.passkeys.api.CheckPasskeySupport
import proton.android.pass.passkeys.api.GeneratePasskey
import proton.android.pass.passkeys.api.ParseCreatePasskeyRequest

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesPasskeysModule {

    @Binds
    abstract fun bindAuthenticateWithPasskey(impl: FakeAuthenticateWithPasskey): AuthenticateWithPasskey

    @Binds
    abstract fun bindGeneratePasskey(impl: FakeGeneratePasskey): GeneratePasskey

    @Binds
    abstract fun bindParseCreatePasskeyRequest(impl: FakeParseCreatePasskeyRequest): ParseCreatePasskeyRequest

    @Binds
    abstract fun bindCheckPasskeySupport(impl: FakeCheckPasskeySupport): CheckPasskeySupport
}

