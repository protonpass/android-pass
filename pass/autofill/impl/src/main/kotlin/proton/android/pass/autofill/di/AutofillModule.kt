/*
 * Copyright (c) 2025 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.autofill.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.autofill.ThirdPartyModeProvider
import proton.android.pass.autofill.ThirdPartyModeProviderImpl
import proton.android.pass.autofill.api.suggestions.PackageNameUrlSuggestionAdapter
import proton.android.pass.autofill.extensions.PackageNameUrlSuggestionAdapterImpl
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
internal abstract class AutofillModule {

    @[Binds Singleton]
    internal abstract fun bindPackageNameUrlSuggestionAdapter(
        impl: PackageNameUrlSuggestionAdapterImpl
    ): PackageNameUrlSuggestionAdapter

    @[Binds Singleton]
    internal abstract fun bindThirdPartyModeProvider(impl: ThirdPartyModeProviderImpl): ThirdPartyModeProvider

}
