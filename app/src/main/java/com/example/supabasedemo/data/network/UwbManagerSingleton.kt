import android.content.Context
import android.util.Log
import androidx.core.uwb.RangingParameters
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


object UwbManagerSingleton {

    private var uwbManager: UwbManager? = null
    private var clientSessionScope: UwbClientSessionScope? = null
    private var controllerSessionScope: UwbControllerSessionScope? = null
    private var controleeSessionScope: UwbControleeSessionScope? = null
    private var isController: Boolean = true
    private var isStarted: Boolean = false
    private var distance: Double = -1.0
    private var azimuth: Double = -1.0

    private var initializationDeferred: CompletableDeferred<Unit>? = null

    fun initialize(context: Context) {
        uwbManager = UwbManager.createInstance(context)
        initializationDeferred = CompletableDeferred()

        CoroutineScope(Dispatchers.Main.immediate).launch {
            try {
                controllerSessionScope = uwbManager?.controllerSessionScope()
                controleeSessionScope = uwbManager?.controleeSessionScope()

                if (controllerSessionScope == null) {
                    Log.e("uwb", "Controller session not initialized")
                }
                if (controleeSessionScope == null) {
                    Log.e("uwb", "Controlee session not initialized")
                }

                initializationDeferred?.complete(Unit)
                Log.d("uwb", "Device address ${getDeviceAddress()}")
                Log.d("uwb", "Device preamble ${getDevicePreamble()}")
            } catch (e: Exception) {
                Log.e("uwb", "Error during session initialization: ${e.message}")
                initializationDeferred?.completeExceptionally(e)
            }
        }
    }

    fun setController(isController: Boolean) {
        this.isController = isController
    }

    fun startSession(address: String, preamble: String) {
        if (isStarted) return

        runBlocking { waitForInitialization() }

        Log.d(
            "uwb",
            "Starting UWB session - Address: $address Preamble: $preamble IsController: $isController DeviceAddress: ${getDeviceAddress()} DevicePreamble: ${getDevicePreamble()}"
        )

        val partnerAddress = UwbAddress(Shorts.toByteArray(address.toShort()))

        val sessionScope: UwbClientSessionScope?
        val uwbComplexChannel: UwbComplexChannel?


        if (isController) {
            sessionScope = controllerSessionScope
            uwbComplexChannel = controllerSessionScope?.uwbComplexChannel
        } else {
            sessionScope = controleeSessionScope
            uwbComplexChannel = UwbComplexChannel(9, preamble.toInt())
        }

        if (sessionScope == null) {
            Log.e(
                "uwb",
                "sessionScope is null for ${if (isController) "Controller" else "Controlee"}"
            )
            return
        }

        val rangingParameters = RangingParameters(
            uwbConfigType = RangingParameters.CONFIG_UNICAST_DS_TWR,
            sessionId = 12345,
            subSessionId = 0,
            sessionKeyInfo = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            subSessionKeyInfo = null,
            complexChannel = uwbComplexChannel,
            peerDevices = listOf(UwbDevice(partnerAddress)),
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_FREQUENT
        )

        val sessionFlow = sessionScope.prepareSession(rangingParameters)

        isStarted = true
        Log.d("uwb", "Totally UWB Session started")
        CoroutineScope(Dispatchers.Main.immediate).launch {
            Log.d("uwb", "Collecting session flow")
            try {
                sessionFlow.collect { result ->
                    Log.d("uwb-result", "Result: $result")
                    when (result) {
                        is RangingResultPosition -> {
                            // Process position and distance
                            distance = result.position.distance?.value?.toDouble() ?: -1.0
                            azimuth = result.position.azimuth?.value?.toDouble() ?: -1.0

                            Log.d("uwb", "Distance: $distance Azimuth: $azimuth")
                        }

                        is RangingResultPeerDisconnected -> {
                            Log.e("uwb", "Session flow disconnected event: $result.")
                            stopSession()
                        }

                        else -> {
                            Log.e("uwb", "Unexpected session flow event: $result")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("uwb", "Error collecting session flow: ${e.message}", e)
                stopSession()
            }
        }
    }

    fun stopSession() {
        if (!isStarted) return
        isStarted = false
        Log.d("uwb", "Stopping UWB session")
        CoroutineScope(Dispatchers.IO).launch {
            clientSessionScope = if (isController) {
                uwbManager?.controllerSessionScope()
            } else {
                uwbManager?.controleeSessionScope()
            }
        }
    }

    fun getDistance(): Double = distance
    fun getAzimuth(): Double = azimuth

    fun getDeviceAddress(): String {
        return if (isController) {
            controllerSessionScope?.localAddress?.toString() ?: "Controller session not initialized"
        } else {
            controleeSessionScope?.localAddress?.toString() ?: "Controlee session not initialized"
        }
    }

    fun getDevicePreamble(): String {
        return if (isController) {
            controllerSessionScope?.uwbComplexChannel?.preambleIndex?.toString()
                ?: "Preamble not available"
        } else {
            "Preamble is not required for Controlee"
        }
    }

    suspend fun waitForInitialization() {
        initializationDeferred?.await()
    }

    suspend fun getDeviceAddressSafe(): Short {
        waitForInitialization()
        return if (isController) {
            Shorts.fromByteArray(controllerSessionScope?.localAddress?.address)
        } else {
            Shorts.fromByteArray(controleeSessionScope?.localAddress?.address)
        }
    }

    suspend fun getDevicePreambleSafe(): String? {
        waitForInitialization()
        return if (isController) {
            controllerSessionScope?.uwbComplexChannel?.preambleIndex?.toString()
        } else {
            "N/A"
        }
    }
}
