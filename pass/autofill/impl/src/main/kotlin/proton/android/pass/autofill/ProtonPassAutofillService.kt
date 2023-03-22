package proton.android.pass.autofill

import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import dagger.hilt.android.AndroidEntryPoint
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@AndroidEntryPoint
class ProtonPassAutofillService : AutofillService() {

    @Inject
    lateinit var autofillServiceManager: AutofillServiceManager

    @Inject
    lateinit var telemetryManager: TelemetryManager

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        AutoFillHandler.handleAutofill(
            context = this@ProtonPassAutofillService,
            request = request,
            callback = callback,
            cancellationSignal = cancellationSignal,
            autofillServiceManager = autofillServiceManager,
            telemetryManager = telemetryManager
        )
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        AutoSaveHandler.handleOnSave(this, request, callback)
    }
}
