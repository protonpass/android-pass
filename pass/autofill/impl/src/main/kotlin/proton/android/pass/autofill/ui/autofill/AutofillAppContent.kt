/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.autofill.ui.autofill

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.coroutines.Job
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.ui.autofill.common.AutofillConfirmMode
import proton.android.pass.autofill.ui.autofill.common.ConfirmAutofillDialog
import proton.android.pass.autofill.ui.autofill.select.AssociateAutofillItemDialog
import proton.android.pass.commonui.api.onBottomSheetDismissed
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.features.auth.AUTH_GRAPH
import proton.android.pass.features.selectitem.navigation.SelectItem
import proton.android.pass.navigation.api.rememberAppNavigator
import proton.android.pass.navigation.api.rememberBottomSheetNavigator

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialNavigationApi::class
)
@Composable
fun AutofillAppContent(
    modifier: Modifier = Modifier,
    autofillAppState: AutofillAppState,
    selectedAutofillItem: AutofillItem?,
    needsAuth: Boolean,
    onNavigate: (AutofillNavigation) -> Unit,
    viewModel: AutofillAppViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.setHadSelectedAutofillItem(selectedAutofillItem != null)
    }

    var showAssociateDialog: ItemUiModel? by remember { mutableStateOf(null) }
    var showWarningDialog: ItemUiModel? by remember { mutableStateOf(null) }

    val event by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(event) {
        when (val e = event) {
            AutofillAppEvent.Cancel -> {
                onNavigate(AutofillNavigation.Cancel)
            }
            is AutofillAppEvent.SendResponse -> {
                onNavigate(AutofillNavigation.SendResponse(e.mappings))
            }
            is AutofillAppEvent.ShowAssociateDialog -> {
                showAssociateDialog = e.item
            }
            is AutofillAppEvent.ShowWarningDialog -> {
                showWarningDialog = e.item
            }
            AutofillAppEvent.Unknown -> {}
        }
        viewModel.clearEvent()
    }

    val startDestination = remember {
        if (needsAuth) {
            AUTH_GRAPH
        } else {
            SelectItem.route
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    val appNavigator = rememberAppNavigator(
        bottomSheetNavigator = rememberBottomSheetNavigator(bottomSheetState)
    )
    val bottomSheetJob: MutableState<Job?> = remember { mutableStateOf(null) }

    PassModalBottomSheetLayout(bottomSheetNavigator = appNavigator.passBottomSheetNavigator) {
        NavHost(
            modifier = modifier.defaultMinSize(minHeight = 200.dp),
            navController = appNavigator.navController,
            startDestination = startDestination
        ) {
            autofillActivityGraph(
                appNavigator = appNavigator,
                autofillAppState = autofillAppState,
                selectedAutofillItem = selectedAutofillItem,
                onNavigate = onNavigate,
                onEvent = {
                    when (it) {
                        is AutofillEvent.AutofillItemSelected -> {
                            viewModel.onItemSelected(
                                state = autofillAppState,
                                autofillItem = it.item,
                                isSuggestion = false
                            )
                        }
                        is AutofillEvent.AutofillSuggestionSelected -> {
                            viewModel.onItemSelected(
                                state = autofillAppState,
                                autofillItem = it.item,
                                isSuggestion = true
                            )
                        }
                        AutofillEvent.SelectItemScreenShown -> {
                            viewModel.onSelectItemScreenShown(
                                packageName = autofillAppState.autofillData.packageInfo.packageName
                            )
                        }
                    }
                },
                dismissBottomSheet = { block ->
                    onBottomSheetDismissed(
                        coroutineScope = coroutineScope,
                        modalBottomSheetState = bottomSheetState,
                        dismissJob = bottomSheetJob,
                        block = block
                    )
                }
            )
        }

        showAssociateDialog?.let { itemUiModel ->
            AssociateAutofillItemDialog(
                itemUiModel = itemUiModel,
                onAssociateAndAutofill = {
                    viewModel.onAssociationResult(
                        state = autofillAppState,
                        item = it,
                        associate = true
                    )
                    showAssociateDialog = null
                },
                onAutofill = {
                    viewModel.onAssociationResult(
                        state = autofillAppState,
                        item = it,
                        associate = false
                    )
                    showAssociateDialog = null
                },
                onDismiss = {
                    showAssociateDialog = null
                },
                onCancel = {
                    viewModel.onAssociationCancelled(
                        isInlineSuggestionSession = selectedAutofillItem != null
                    )
                }
            )
        }

        showWarningDialog?.let { itemUiModel ->
            ConfirmAutofillDialog(
                mode = AutofillConfirmMode.DangerousAutofill,
                onConfirm = {
                    viewModel.onWarningConfirmed(
                        state = autofillAppState,
                        item = itemUiModel
                    )
                    showWarningDialog = null
                },
                onClose = {
                    showWarningDialog = null
                }
            )
        }
    }
}
