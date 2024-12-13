import android.content.Context
import android.util.Log
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingResult
import androidx.core.uwb.RangingResult.RangingResultPeerDisconnected
import androidx.core.uwb.RangingResult.RangingResultPosition
import androidx.core.uwb.UwbAddress
import androidx.core.uwb.UwbClientSessionScope
import androidx.core.uwb.UwbComplexChannel
import androidx.core.uwb.UwbControleeSessionScope
import androidx.core.uwb.UwbControllerSessionScope
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import com.google.common.primitives.Shorts
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


object UwbManagerSingleton {

    private var uwbManager: UwbManager? = null
    private var sessionScope: UwbClientSessionScope? = null
    private var isController: Boolean = true

    private var isStarted: Boolean = false
    private val _isStartedFlow = MutableStateFlow(false)
    val isStartedFlow: StateFlow<Boolean> = _isStartedFlow

    private val _address = MutableStateFlow("-2")
    val address: StateFlow<String> get() = _address
    private val _preamble = MutableStateFlow("-2")
    val preamble: StateFlow<String> get() = _preamble

    private var distance: Double = -1.0
    private var azimuth: Double = -1.0

    private var initializationDeferred: CompletableDeferred<Unit>? = null

    fun initialize(context: Context, isController: Boolean) {
        stopSession()
        this.isController = isController
        uwbManager = UwbManager.createInstance(context)
        initializationDeferred = CompletableDeferred()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                sessionScope = if (isController) {
                    uwbManager?.controllerSessionScope()
                } else {
                    uwbManager?.controleeSessionScope()
                }

                if (sessionScope == null) throw IllegalStateException("Session initialization failed")
                initializationDeferred?.complete(Unit)
                fetchDeviceDetails()
                Log.d("uwb", "Initialized as ${if (isController) "Controller" else "Controlee"}")
            } catch (e: Exception) {
                Log.e("uwb", "Error during session initialization: ${e.message}")
                initializationDeferred?.completeExceptionally(e)
            }
        }
    }

    fun setRoleAsController(isController: Boolean, context: Context) {
        if (this.isController != isController) {
            initialize(context, isController)
        }
    }

    fun startSession(partnerAddress: String, preamble: String) {
        if (_isStartedFlow.value) return

        runBlocking { waitForInitialization() }

        Log.d(
            "uwb",
            "Starting UWB session - Address: $partnerAddress Preamble: $preamble IsController: $isController DeviceAddress: ${getDeviceAddress()} DevicePreamble: ${getDevicePreamble()}"
        )

        val partnerUwbAddress = UwbAddress(Shorts.toByteArray(partnerAddress.toShort()))

        val uwbComplexChannel = if (isController) {
            (sessionScope as? UwbControllerSessionScope)?.uwbComplexChannel
        } else {
            UwbComplexChannel(9, preamble.toInt())
        }

        if (sessionScope == null) {
            Log.e(
                "uwb",
                "Session scope is null for ${if (isController) "Controller" else "Controlee"}"
            )
            return
        }

        val rangingParameters = RangingParameters(
            uwbConfigType = RangingParameters.CONFIG_UNICAST_DS_TWR,
            sessionId = 12345,
            subSessionId = 0,
            sessionKeyInfo = ByteArray(8),
            subSessionKeyInfo = null,
            complexChannel = uwbComplexChannel,
            peerDevices = listOf(UwbDevice(partnerUwbAddress)),
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_FREQUENT
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                sessionScope?.prepareSession(rangingParameters)?.collect { result ->
                    handleRangingResult(result)
                }
            } catch (e: Exception) {
                Log.e("uwb", "Error during session collection: ${e.message}", e)
                stopSession()
            }
        }

        isStarted = true
        _isStartedFlow.value = true
        Log.d("uwb", "Session started successfully.")
    }

    private fun handleRangingResult(result: RangingResult) {
        when (result) {
            is RangingResultPosition -> {
                distance = result.position.distance?.value?.toDouble() ?: -1.0
                azimuth = result.position.azimuth?.value?.toDouble() ?: -1.0
                Log.d("uwb", "Distance: $distance, Azimuth: $azimuth")
            }

            is RangingResultPeerDisconnected -> {
                Log.e("uwb", "Peer disconnected: $result")
                stopSession()
            }

            else -> {
                Log.e("uwb", "Unexpected result: $result")
            }
        }
    }

    fun stopSession() {
        if (!isStarted) return
        if (!_isStartedFlow.value) return

        isStarted = false
        _isStartedFlow.value = false

        Log.d("uwb", "Stopping UWB session")
        sessionScope = null
    }

    fun getDistance(): Double = distance
    fun getAzimuth(): Double = azimuth

    private suspend fun waitForInitialization() {
        initializationDeferred?.await()
    }

    private fun getDeviceAddress(): String {
        return sessionScope?.localAddress?.toString() ?: "Session scope not initialized"
    }

    private fun getDevicePreamble(): String {
        return if (isController) {
            (sessionScope as? UwbControllerSessionScope)?.uwbComplexChannel?.preambleIndex?.toString()
                ?: "Preamble not available"
        } else {
            "N/A for Controlee"
        }
    }

    fun updateAddress(newAddress: String) {
        _address.value = newAddress
    }

    fun updatePreamble(newPreamble: String) {
        _preamble.value = newPreamble
    }

    suspend fun getDeviceAddressSafe(): Short {
        waitForInitialization()
        return Shorts.fromByteArray(sessionScope?.localAddress?.address)
    }

    suspend fun getDevicePreambleSafe(): String? {
        waitForInitialization()
        return if (isController) {
            (sessionScope as? UwbControllerSessionScope)?.uwbComplexChannel?.preambleIndex?.toString()
        } else {
            "N/A"
        }
    }

    suspend fun fetchDeviceDetails() {
        val newAddress = getDeviceAddressSafe()
        val newPreamble = getDevicePreambleSafe()

        updateAddress(newAddress.toString())
        updatePreamble(newPreamble ?: "N/A")
    }
}
