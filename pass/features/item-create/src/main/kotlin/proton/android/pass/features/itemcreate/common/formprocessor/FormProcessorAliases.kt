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

package proton.android.pass.features.itemcreate.common.formprocessor

import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.creditcard.CreditCardItemFormState
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.ItemFormState
import proton.android.pass.features.itemcreate.identity.presentation.IdentityItemFormState
import proton.android.pass.features.itemcreate.login.LoginItemFormState
import proton.android.pass.features.itemcreate.note.NoteItemFormState

typealias CreditCardFormProcessorType =
    FormProcessor<CreditCardItemFormProcessor.Input, CreditCardItemFormState>
typealias LoginItemFormProcessorType =
    FormProcessor<LoginItemFormProcessor.Input, LoginItemFormState>
typealias CustomItemFormProcessorType =
    FormProcessor<CustomItemFormProcessor.Input, ItemFormState>
typealias IdentityItemFormProcessorType =
    FormProcessor<IdentityItemFormProcessor.Input, IdentityItemFormState>
typealias NoteItemFormProcessorType =
    FormProcessor<NoteItemFormProcessor.Input, NoteItemFormState>
typealias CustomFieldFormProcessorType =
    FormProcessor<UICustomFieldContentFormProcessor.Input, List<UICustomFieldContent>>
typealias SectionFormProcessorType =
    FormProcessor<UISectionContentFormProcessor.Input, List<UIExtraSection>>
