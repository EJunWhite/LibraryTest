package library.android.support.helper.measurement.location

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import com.google.android.gms.location.LocationAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.withContext
import library.android.support.helper.measurement.location.LocationHandler
import library.android.support.helper.measurement.MeasurementInterface

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class LocationMeasurement constructor(private val locationHandler: LocationHandler):
    MeasurementInterface, LocationHandler.OnLocationChangedCallback {

        companion object {
            val TAG = "LocationMeasurement"
        }

    override fun initMeasurement(context: Context, params: Bundle) {
    }

    override fun pauseMeasurement(context: Context) {
        locationHandler.gpsOff()
    }

    override fun startMeasurement(context: Context) {
        locationHandler.addCallback(context, this)
    }

    override fun stopMeasurement(context: Context) {
    }

    override suspend fun saveMeasurement(context: Context) {
    }

    override suspend fun onDestroyMeasurement(context: Context) {
        withContext(Dispatchers.Main){
            pauseMeasurement(context)
        }
        withContext(Dispatchers.IO){
            saveMeasurement(context)
        }
    }

    override fun onLocationChanged(location: Location?) {
        Log.i(TAG, "onLocationChanged :: $location")
    }

    override fun onLastLocationSuccess(location: Location?) {
        Log.i(TAG, "onLastLocationSuccess :: $location")
    }

    override fun onAvailabilityChanged(locationAvailability: LocationAvailability?) {
        Log.i(TAG, "onAvailabilityChanged :: $locationAvailability")
    }
}