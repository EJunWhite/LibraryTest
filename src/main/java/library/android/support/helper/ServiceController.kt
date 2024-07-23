package library.android.support.helper

import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import library.android.support.helper.measurement.location.LocationMeasurement
import library.android.support.helper.measurement.MeasurementInterface
import library.android.support.helper.measurement.location.LocationHandler

@InternalCoroutinesApi
class ServiceController {
    private val locationMeasurement: LocationMeasurement = LocationMeasurement(LocationHandler())

    private var paramFolderName = ""
    private var paramInternalStorage = false

    private var paramSensorSpeed: Int = SensorManager.SENSOR_DELAY_FASTEST
    private var paramGPSToMeasure: Boolean = false

    fun onInit(context: Context, intent: Intent?) {
        onStart(context)
    }

    fun onStart(context: Context): Boolean {
        val params: Bundle = Bundle().apply {
            putString(MeasurementInterface.FOLDER_NAME, paramFolderName)
            putBoolean(MeasurementInterface.INTERNAL_STORAGE, paramInternalStorage)
        }

        locationMeasurement.initMeasurement(context, params)
        locationMeasurement.startMeasurement(context)

        return true
    }

    suspend fun onStop(context: Context) {
        val saving = CoroutineScope(Dispatchers.Main).launch {
            locationMeasurement.onDestroyMeasurement(context)
        }

        saving.join()
    }
}