package library.android.support.helper.measurement

import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.ExperimentalCoroutinesApi

interface MeasurementInterface {
    fun initMeasurement(context: Context, params: Bundle)
    fun pauseMeasurement(context: Context)
    fun startMeasurement(context: Context)
    fun stopMeasurement(context: Context)

    suspend fun saveMeasurement(context: Context)
    suspend fun onDestroyMeasurement(context: Context)

    companion object {
        const val FOLDER_NAME = "FOLDER_NAME"
        const val SENSOR_ID = "SENSOR_ID"
        const val SENSOR_SPEED = "SENSOR_SPEED"
        const val INTERNAL_STORAGE = "INTERNAL_STORAGE"
    }
}