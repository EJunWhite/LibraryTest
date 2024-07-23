package library.android.support.helper.measurement.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@SuppressLint("MissingPermission")
class LocationHandler : LocationCallback() {
    companion object {
        private val TAG = "GPS_LOCATION"

        val LOOP_TIME: Long = 5000L
    }

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private var callback: OnLocationChangedCallback? = null

    private var locationAvailability: LocationAvailability? = null
    private var lastLocation: Location? = null

    private var registered: Boolean = false
    private var isCreateInit: Boolean = false


    /**
     * creation of the request and locationClient
     *
     * @param context
     */
    private fun createWithInit(context: Context){
        locationRequest = createRequest(context)
        locationClient = LocationServices.getFusedLocationProviderClient(context)
        isCreateInit = true
    }

    /**
     * calls for last known location and registers location callback
     *
     * @param context
     */
    private fun initialize(context: Context) {
        if (!isCreateInit) {
            createWithInit(context)
        }

        locationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location == null) {
                callback!!.onLastLocationSuccess(null)
            } else {
                lastLocation = location
                callback?.onLastLocationSuccess(location)
            }

        }.addOnFailureListener {
            callback!!.onLastLocationSuccess(null)
        }

        locationClient.requestLocationUpdates(locationRequest, this, Looper.getMainLooper())
        registered = true
    }

    /**
     * saves last location and is passed if the new callback registers
     *
     * @param locationResult
     */
    override fun onLocationResult(locationResult: LocationResult) {
        super.onLocationResult(locationResult)
        if (locationResult.locations.size > 0) {
            lastLocation = locationResult.lastLocation
            if (lastLocation != null) {
                callback?.onLocationChanged(lastLocation)
            }
        }
    }

    /**
     * changes if the location cahnges provider / GPS is off
     *
     * @param locationAvailability
     */
    override fun onLocationAvailability(locationAvailability: LocationAvailability) {
        super.onLocationAvailability(locationAvailability)
        this.locationAvailability = locationAvailability
        callback?.onAvailabilityChanged(locationAvailability)
    }

    /**
     * removes GPS - no updates will be passed
     *
     */
    fun gpsOff() {
        if(registered){
            Log.i(TAG, "Logging off location")
            locationClient.flushLocations()
            locationClient.removeLocationUpdates(this)
        }
        registered = false
    }

    private fun createRequest(context: Context): LocationRequest {
        var b: LocationRequest.Builder
        try {
            b = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOOP_TIME)
            b.setIntervalMillis(LOOP_TIME)
            b.setMinUpdateIntervalMillis(LOOP_TIME)
            b.setMaxUpdateAgeMillis(LOOP_TIME)
//            b.setMinUpdateDistanceMeters(sharedPreferences.getString(MeasurementService.GPS_DISTANCE, "20")!!.toFloat())
        } catch (e: ClassCastException) {
            b = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOOP_TIME)
            b.setIntervalMillis(LOOP_TIME)
            b.setMinUpdateIntervalMillis(LOOP_TIME)
            b.setMaxUpdateAgeMillis(LOOP_TIME)
//            b.setMinUpdateDistanceMeters(sharedPreferences.getInt(MeasurementService.GPS_DISTANCE, 20).toFloat())
        }
//        b.setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
//        b.setWaitForAccurateLocation(true)
        //registering GPS
        Log.i("GPS", "location request created")
        return b.build()
    }

    /**
     * adding callback to pass location
     *
     * @param context
     * @param gpsCallback - this object will get access to location and updates, previous is forgotten
     *
     */
    fun addCallback(context: Context, gpsCallback: OnLocationChangedCallback) {
        if(registered){
            gpsOff()
        }

        initialize(context)
        callback = gpsCallback
        Log.i("GPS", "$lastLocation")
        gpsCallback.onLocationChanged(lastLocation)
    }

    interface OnLocationChangedCallback {
        fun onLocationChanged(location: Location?)
        fun onLastLocationSuccess(location: Location?)
        fun onAvailabilityChanged(locationAvailability: LocationAvailability?)
    }
}