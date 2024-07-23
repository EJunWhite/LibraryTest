package library.android.support.helper.measurement.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.core.graphics.drawable.IconCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import library.android.support.helper.NotifyHelper
import library.android.support.helper.ServiceController
import library.android.support.helper.measurement.MeasurementStates

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class MeasurementService: Service() {
    interface OnMeasurementStateListener {
        fun onServiceState(measurementStates: MeasurementStates)
    }

    private val serviceBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(TAG, "intent: ${intent}")
            intent?.let {
                when(it.action) {
                    STOP_SERVICE -> {
                        if (!it.getBooleanExtra(USER, false)) {
                            sendOnFinishNotification()
                        }
                        cancelService()
                    }

                    Intent.ACTION_BATTERY_LOW -> {

                    }

                    else -> {

                    }
                }
            }
        }
    }

    inner class MeasurementBinder : Binder() {
        fun getService(): MeasurementService {
            return this@MeasurementService
        }
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(p0: Intent?): IBinder {
        return MeasurementBinder()
    }

    // can pass information directly to the listener
    // used mainly for the short measurement to pass countdown
    var onMeasurementStateListener: OnMeasurementStateListener? = null

    var running = false
    var intent: Intent? = null
    val startTime: Long = SystemClock.elapsedRealtime()

    private var wakeLock: PowerManager.WakeLock? = null
    private var receiver: Boolean = false
    private var shortJob: Job? = null

    // params
    var paramType: Int = ENDLESS
    var paramSensorId: IntArray? = null
    private var paramInternalStorage = false
    private var paramTimeIntervals: IntArray = intArrayOf(-1)
    private var paramChecks: BooleanArray = booleanArrayOf(false, false, false, false, false)

    // main controller for measurement
    private val serviceController: ServiceController = ServiceController()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Measurement START")
        running = true

//        intent?.let {
//            unwrapIntent(it)
//        }
        serviceController.onInit(this, intent)

        if (!paramInternalStorage) { // show notification at phone only
            onGoingNotification()
        }
        handleAndroid()

        serviceController.onInit(this, intent)

        /*handleAndroid()

        serviceController.onInit(this, intent)

        if (serviceController.onStart(this)) {
            cancelService()
        }

        when (paramType) {
            SHORT -> createShortTimer()
//            LONG -> createLongTimer()
            ENDLESS -> START_REDELIVER_INTENT
        }*/
        return START_NOT_STICKY
    }

    private fun onGoingNotification(title: String = "", body: String = "") {
        var title = if (title.isEmpty()) "운행" else title
        var body = if (body.isEmpty()) "서비스 준비중" else body

        val stopIntent = Intent(STOP_SERVICE)
        val notify = NotifyHelper.createNotification(
            context = applicationContext,
            pendingIntent = NotifyHelper.createPendingIntent(this, stopIntent, 20),
            title = "$title",
            body = "$body",
            smallIcon = IconCompat.createWithResource(applicationContext, library.android.support.R.drawable.ic_notification),
            isAutoCancel = false,
            isOngoing = true,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(100, notify, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(100, notify)
        }
    }

    private fun handleAndroid() {
//        wakeLockHandle()
        createBroadcastReceiver()
    }

    /**
     * picks necessary parameters from the intent
     *
     * @param intent - formated by companion methods below
     */
    private fun unwrapIntent(intent: Intent) {

        this.intent = intent
        intent.extras?.let {
            paramSensorId = it.getIntArray(ANDROID_SENSORS)
            paramType = it.getInt(TYPE, ENDLESS)
            paramTimeIntervals = it.getIntArray(TIME_INTERVALS)!!
            paramChecks = it.getBooleanArray(OTHER)!!
            paramInternalStorage = it.getBoolean(INTERNAL_STORAGE)
        }
    }

    /**
     * registers receiver for the intent to stop service / write annotation
     * also can registers intents for battery status and controls the battery %
     */
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun createBroadcastReceiver() {
        val intentFilter = IntentFilter(STOP_SERVICE)
        intentFilter.addAction(ANNOTATION)
        intentFilter.addAction(STATUS)

        if (paramChecks[2]) {
            intentFilter.addAction(Intent.ACTION_BATTERY_LOW)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(serviceBroadcastReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(serviceBroadcastReceiver, intentFilter)
        }

        receiver = true
    }

    /**
     * acquires wakelock with time limit for LONG/SHORT measurement
     *
     */
    @SuppressLint("WakelockTimeout")
    private fun wakeLockHandle() {
        if (paramChecks[3]) { // wakelock flag
            wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SensorBox::Measuring").apply {
                    when (paramType) {
                        SHORT -> {
                            acquire(paramTimeIntervals[1] * 1000L)
                        }
//                        LONG -> {
//                            acquire(((paramTimeIntervals[0] * 3600 + paramTimeIntervals[1] * 60) * 1000).toLong())
//                        }
                        else -> {
                            acquire()
                        }
                    }
                }
            }
        }
    }

    private fun createShortTimer() {
        /*shortJob = CoroutineScope(Dispatchers.Default).launch {
            serviceController.shortTimer().onEach { state ->
                withContext(Dispatchers.Main) {

                    if (state is MeasurementStates.OnShortEnd) { // on the end of the countdown
                        onMeasurementStateListener?.onServiceState(
                            MeasurementStates.OnEndMeasurement(
                                paramType,
                                paramChecks[4] // repetition
                            )
                        )
                        onMeasurementStateListener?.onServiceState(MeasurementStates.StateNothing)
                        sendOnFinishNotification()
                        serviceController.onStop(this@MeasurementService)
                        cancelService()
                    } else {
                        // passing seconds left
                        onMeasurementStateListener?.onServiceState(state)
                    }

                }
            }.launchIn(this)
        }*/

    }


    /**
     * finishing notification after stopping of the service
     *
     */
    private fun sendOnFinishNotification() {
        // TODO: need to upgrade
//        NotifyHelper.updateNotification(
//            this, FINISH_NOTIFICATION,
//            NotifyHelper.endingNotification(
//                this,
//                getString(R.string.notification_finish_title),
//                getString(R.string.notification_finish_content)
//            )
//        )
    }

    /**
     * method stop measurement from the service
     *
     */
    private fun cancelService() {
        if (running) {
            running = false

            onMeasurementStateListener?.onServiceState( // ending to activity
                MeasurementStates.OnEndMeasurement(
                    paramType,
                    paramChecks[4]
                )
            )

            onMeasurementStateListener?.onServiceState(MeasurementStates.StateNothing)

            shortJob?.cancel()

            val savingJob = CoroutineScope(Dispatchers.Main).launch {
                serviceController.onStop(this@MeasurementService)
            } // stops measurement
            savingJob.invokeOnCompletion {
                stopForeground(STOP_FOREGROUND_REMOVE)
                onMeasurementStateListener = null
                stopSelf()
            }

        } else {
            stopSelf()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        stopForeground(STOP_FOREGROUND_REMOVE)

        if (wakeLock != null) {
            if (wakeLock!!.isHeld) {
                wakeLock!!.release()
            }
        }

        if (receiver) {
            unregisterReceiver(serviceBroadcastReceiver)
            receiver = false
        }
        running = false
    }


    companion object {
        const val TAG = "MeasurementService"
        // notifications

        private var notification: Notification? = null

        // parameters
        const val FOLDER_NAME = "FOLDER_NAME"
        const val CUSTOM_NAME = "CUSTOM_NAME"
        const val INTERNAL_STORAGE = "INTERNAL_STORAGE"
        const val OTHER = "OTHER"
        private const val ALARMS = "ALARMS"
        internal const val NOTES = "NOTES"
        const val TIME_INTERVALS = "TIME_INTERVALS"
        const val ANDROID_SENSORS = "ANDROID_SENSORS"
        const val ANDROID_SENSORS_SPEED = "ANDROID_SENSORS_SPEED"
        const val GPS = "GPS_MEASURE"
        const val TYPE = "TYPE"
        const val WEAR_SENSORS = "WEAR_SENSORS"

        // types
        const val SHORT = 0
        const val ENDLESS = 1
//        const val LONG = 2

        const val SHORT_STRING = "SHORT"
        const val ENDLESS_STRING = "ENDLESS"
        const val LONG_STRING = "LONG"


        // intents
        const val USER = "USER"
        const val STOP_ACTIVITY = "STOP_ACTIVITY"
        const val STOP_SERVICE = "STOP_SERVICE"
        const val ANNOTATION = "ANNOTATION"

        const val STATUS = "STATUS"
        const val RUNNING = "RUNNING"
        const val ANNOTATION_TIME = "ANNOTATION_TIME"
        const val ANNOTATION_TEXT = "ANNOTATION_TEXT"

        /**
         * used for basic measurement in HomeFragment
         *
         * @param intent - intent for MeasurementService
         * @param internalStorage - to use in Wear Os - true
         * @param sensorsToMeasure // default
         * @param gpsToMeasure - true to use GPS
         * @param nameOfFolder - string with formatted name ENDLESS_10_9_2020_20_50_30
         * @param wearSensors - intArray of sensor Ids for Wear Os - can be null
         * @return filled intent with extras based on string on top of the companion object
         */
        fun addExtraToIntentBasic(
            intent: Intent,
            internalStorage: Boolean,
            sensorsToMeasure: IntArray,
            gpsToMeasure: Boolean,
            nameOfFolder: String,
            wearSensors: IntArray?
        ): Intent {
            return intent.apply {
                putExtra(FOLDER_NAME, nameOfFolder)
                putExtra(INTERNAL_STORAGE, internalStorage)
                putExtra(ANDROID_SENSORS, sensorsToMeasure)
                putExtra(WEAR_SENSORS, wearSensors)
                putExtra(ANDROID_SENSORS_SPEED, SensorManager.SENSOR_DELAY_FASTEST) // default
                putExtra(GPS, gpsToMeasure)
                putExtra(TYPE, ENDLESS)
                putExtra(TIME_INTERVALS, intArrayOf(-1)) // not used
                putExtra(NOTES, arrayListOf("")) // not used
                putExtra(ALARMS, intArrayOf(-1)) // not used
                putExtra(OTHER, booleanArrayOf(false, false, false, false, false)) // default
            }

        }

        /**
         * used in Advanced Measurement to pack all the parameters
         *
         * @param intent - intent to MeasurementService
         * @param folder - whole folder name
         * @param customName - pure custom string with name for folder
         * @param internalStorage - to use in Wear Os, this should be true
         * @param sensorsToMeasure - intArray with sensors Ids to measure
         * @param speedSensor - SensorManager.SENSOR_DELAY_FAST, ...
         * @param gpsToMeasure - true to measure GPS
         * @param typeMeasurement - SHORT / ENDLESS / LONG
         * @param timeIntervals - SHORT [time to start in seconds, time to measure in seconds], LONG [hours, minutes]
         * @param notes - arrayList of strings to store
         * @param alarms - arrayList of ints with seconds in which to launch alarms
         * @param checkCheckBoxes - another parameters - [extra_checkbox_activity, significant_motion, battery, cpu, repeat]
         * @param wearSensors - intArray of ids for sensors in Wear Os
         * @return - filled intent
         */
        fun addExtraToIntentAdvanced(
            intent: Intent,
            folder: String,
            customName: String,
            internalStorage: Boolean,
            sensorsToMeasure: IntArray,
            speedSensor: Int,
            gpsToMeasure: Boolean,
            typeMeasurement: Int,
            timeIntervals: Array<Int>,
            notes: ArrayList<String>,
            alarms: ArrayList<Int>,
            checkCheckBoxes: Array<Boolean>,
            wearSensors: IntArray?
        ): Intent {

            return intent.apply {
                putExtra(FOLDER_NAME, folder)
                putExtra(CUSTOM_NAME, customName)
                putExtra(INTERNAL_STORAGE, internalStorage)
                putExtra(ANDROID_SENSORS, sensorsToMeasure)
                putExtra(ANDROID_SENSORS_SPEED, speedSensor)
                putExtra(GPS, gpsToMeasure)
                putExtra(TYPE, typeMeasurement)
                putExtra(TIME_INTERVALS, timeIntervals.toIntArray())
                putExtra(NOTES, notes)
                putExtra(ALARMS, alarms.toIntArray())
                putExtra(OTHER, checkCheckBoxes.toBooleanArray())
                wearSensors?.let {
                    putExtra(WEAR_SENSORS, wearSensors)
                }
            }
        }

        /**
         * Wear Os intent - customized for specific needs - creates ENDLESS measurement
         *
         * @param context
         * @param path - to store data
         * @param sensors - intArray of sensors to use
         * @param sensorSpeed - SensorManager.SENSOR_DELAY_FAST, ...
         * @param battery - stop on low battery - true
         * @param wakeLock - lock the cpu - true
         * @return filled Intent with  params
         */
        fun getIntentWearOs(
            context: Context,
            path: String,
            sensors: IntArray,
            sensorSpeed: Int,
            gps: Boolean,
            battery: Boolean,
            wakeLock: Boolean
        ): Intent {
            val intent = Intent(context, MeasurementService::class.java)

            return addExtraToIntentAdvanced(
                intent,
                path,
                "",
                true,
                sensors,
                sensorSpeed,
                gps,
                ENDLESS,
                arrayOf(-1),
                arrayListOf(""),
                arrayListOf(-1),
                arrayOf(
                    false,
                    false,
                    battery,
                    wakeLock,
                    false
                ),
                null
            )
        }

        // settings keys
        const val ACTIVITY_RECOGNITION_PERIOD = "activity_recognition_period"
        const val GPS_DISTANCE = "gps_distance"
        const val GPS_TIME = "gps_time"

        val PREFS = arrayOf(
            ACTIVITY_RECOGNITION_PERIOD,
            GPS_DISTANCE,
            GPS_TIME
        )

    }



}