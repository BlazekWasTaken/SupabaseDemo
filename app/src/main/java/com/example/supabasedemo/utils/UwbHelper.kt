package com.example.supabasedemo.utils

import android.content.Context
import androidx.core.uwb.UwbAddress
import androidx.core.uwb.UwbClientSessionScope
import androidx.core.uwb.UwbControleeSessionScope
import androidx.core.uwb.UwbControllerSessionScope
import androidx.core.uwb.UwbManager
import com.google.common.primitives.Shorts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

class UwbHelper(
    val scope: CoroutineScope,
    context: Context,
    val isController: Boolean
) {
    private val uwbManager: UwbManager = UwbManager.createInstance(context)
    val currentUwbSessionScope = AtomicReference<UwbClientSessionScope>()

    init {
        scope.launch {
            if (isController) currentUwbSessionScope.set(uwbManager.controllerSessionScope())
            else currentUwbSessionScope.set(uwbManager.controleeSessionScope())
        }
    }

    fun getUwbInfo(): UwbInfo {
        var uwbInfo: UwbInfo? = null
        scope.launch {
            if (isController) {
                val controller = currentUwbSessionScope.get() as UwbControllerSessionScope
                uwbInfo = UwbInfo(
                    address = Shorts.fromByteArray(controller.localAddress.address),
                    complexChannel = controller.uwbComplexChannel.channel,
                    complexChannelPreamble = controller.uwbComplexChannel.preambleIndex,
                    isDistanceSupported = controller.rangingCapabilities.isDistanceSupported,
                    isAzimuthSupported = controller.rangingCapabilities.isAzimuthalAngleSupported,
                    isElevationSupported = controller.rangingCapabilities.isElevationAngleSupported
                )
            }
            else {
                val controlee = currentUwbSessionScope.get() as UwbControleeSessionScope
                uwbInfo = UwbInfo(
                    address = Shorts.fromByteArray(controlee.localAddress.address),
                    complexChannel = null,
                    complexChannelPreamble = null,
                    isDistanceSupported = controlee.rangingCapabilities.isDistanceSupported,
                    isAzimuthSupported = controlee.rangingCapabilities.isAzimuthalAngleSupported,
                    isElevationSupported = controlee.rangingCapabilities.isElevationAngleSupported
                )
            }
        }
        return uwbInfo as UwbInfo
    }

    fun startRanging(
        otherUwbInfo: UwbInfo
    ) {
        val partnerAddress: UwbAddress = UwbAddress(Shorts.toByteArray(otherUwbInfo.address))
    }


}

class UwbInfo(
    val address: Short,
    val complexChannel: Int?,
    val complexChannelPreamble: Int?,
    val isDistanceSupported: Boolean,
    val isAzimuthSupported: Boolean,
    val isElevationSupported: Boolean
)