/*
 * Created by EJun on 2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package library.android.support.helper

import android.app.ActivityManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.ExifInterface
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.CombinedVibration
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Base64
import android.util.Base64OutputStream
import android.util.DisplayMetrics
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity


val TAG: String by lazy { "supportLib" }

class AndroidHelper private constructor() {
    companion object {
        private var isVibrationOff = true
        private var isMediaSoundOff = true

        private var instance: AndroidHelper? = null

        fun getInstance(): AndroidHelper {
            return instance ?: synchronized(this) {
                instance ?: AndroidHelper().also {
                    instance = it
                }
            }
        }
    }

    fun hideKeyboard(context: Context, windowToken: IBinder) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return context.resources.getDimensionPixelSize(resourceId)
    }

    fun getNavigationBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return context.resources.getDimensionPixelSize(resourceId)
    }

    fun setAndroidSupport(vibrationOff: Boolean = true, mediaSoundOff: Boolean = true) {
        isVibrationOff = vibrationOff
        isMediaSoundOff = mediaSoundOff
    }

    fun getTopActivity(context: Context): String {
//        val topActivityName = info?.shortClassName?.substring(1)
        var am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var cn = am.getRunningTasks(1)[0].topActivity?.shortClassName

        return cn.toString()
    }

    fun isServiceRunning(context: Context, serviceName: String): Boolean {
        var manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (runServiceInfo in manager!!.getRunningServices(Int.MAX_VALUE)) {
            Log.d(TAG, "SERVICE :: runServiceInfo.service.className :: ${runServiceInfo.service.className}")
            if (serviceName == runServiceInfo.service.className) {
                return true
            }
        }
        return false
    }

    @RequiresPermission(value = "android.permission.VIBRATE")
    fun performHapticFeedback(context: Context, vibrationEffect: VibrationEffect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibrator = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val combinedVibration = CombinedVibration.createParallel(vibrationEffect)
            vibrator.vibrate(combinedVibration)
            // SDK 26
        } else {
            val vibrator = context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(vibrationEffect)
        }
    }

    fun playMp3(context: Context, rawRes: Int, listener: ChangeAudioVolumeListener) {
        val mediaPlayer = MediaPlayer.create(context, rawRes)
        mediaPlayer.setOnCompletionListener {
            listener.endChange()
        }
        listener.startChange()
        mediaPlayer.start()
    }

    interface ChangeAudioVolumeListener {
        fun startChange()
        fun endChange()
    }

    fun getMusicVolume(context: Context): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    fun setMusicVolume(context: Context, vol: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
    }

    fun isActiveHeadset(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager!!.getDevices(AudioManager.GET_DEVICES_INPUTS)
            val types = listOf(AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_WIRED_HEADSET)
            devices.any {it.type in types}
        } else {
            audioManager!!.isWiredHeadsetOn
        }
    }

    private fun rotateImageIfRequired(context: Context, bitmap: Bitmap, uri: Uri): Bitmap? {
        val input = context.contentResolver.openInputStream(uri) ?: return null

        val exif = if (Build.VERSION.SDK_INT > 23) {
            ExifInterface(input)
        } else {
            ExifInterface(uri.path!!)
        }

        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
            else -> bitmap
        }
    }

    private fun rotateImage(bitmap: Bitmap, degree: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * dp를 px로 변환
     */
    fun dp2px(context: Context, dp: Float): Float {
        val resources: Resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    /**
     * px를 dp로 변환
     */
    fun px2dp(context: Context, px: Float): Float {
        val resources = context.resources
        val metrics: DisplayMetrics = resources.displayMetrics
        val result = px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)

        Log.i("BUS","result $result metrics.densityDpi.toFloat() ${metrics.densityDpi.toFloat()}  DisplayMetrics.DENSITY_DEFAULT ${DisplayMetrics.DENSITY_DEFAULT}")

        return result
    }

    fun base64Decode(base64: String): String {
        val decodedString = Base64.decode(base64, Base64.DEFAULT)
        return String(decodedString)
    }

    fun base64Encode(string: String): String {
        val encodedBytes = Base64.encodeToString(string.toByteArray(), Base64.DEFAULT)
        return encodedBytes
    }

    fun getAvailableMemory(context: Context): ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return ActivityManager.MemoryInfo().also { memoryInfo ->
            activityManager.getMemoryInfo(memoryInfo)
        }
    }
}
