package me.proton.pass.autofill

import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.pass.data.api.crypto.EncryptionContextProvider
import me.proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import javax.inject.Inject

@AndroidEntryPoint
class ProtonPassAutofillService : AutofillService() {

    @Inject
    lateinit var getSuggestedLoginItems: GetSuggestedLoginItems

    @Inject
    lateinit var encryptionContextProvider: EncryptionContextProvider

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        AutoFillHandler.handleAutofill(
            context = this@ProtonPassAutofillService,
            encryptionContextProvider = encryptionContextProvider,
            request = request,
            callback = callback,
            cancellationSignal = cancellationSignal,
            getSuggestedLoginItems = getSuggestedLoginItems
        )
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        AutoSaveHandler.handleOnSave(this, request, callback)
    }
}
