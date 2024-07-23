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

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class SettingHelper private constructor() {
    companion object {
        private var instance: SettingHelper? = null

        fun getInstance(): SettingHelper {
            return instance ?: synchronized(this) {
                instance ?: SettingHelper().also {
                    instance = it
                }
            }
        }
    }

    fun isAirModeOn(context: Context): Boolean {
        return Settings.System.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
    }

    fun isWifiOn(context: Context): Boolean {
        return Settings.System.getInt(context.contentResolver, Settings.Global.WIFI_ON, 0) == 1
    }

    fun isOverlay(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) true else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            Settings.canDrawOverlays(context)
        } else {
            if (Settings.canDrawOverlays(context)) return true
            try {
                val mgr = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager ?: return false
                val viewToAdd = View(context)
                val params = WindowManager.LayoutParams(
                    0,
                    0,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT
                )
                viewToAdd.layoutParams = params
                mgr.addView(viewToAdd, params)
                mgr.removeView(viewToAdd)
                return true
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            false
        }
    }

    fun isIgnoreBatteryOptimization(context: Context, packageName: String): Boolean {
        val powerManager = context.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager

        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    fun isRequestPackageInstall(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return context.packageManager.canRequestPackageInstalls()
        }
        return false
    }

    /**
     * System Notification
     */
    fun isNotificationEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * open setting
     *
     * Settings.ACTION_SETTINGS
     * Settings.ACTION_WIFI_SETTINGS
     * Settings.ACTION_BLUETOOTH_SETTINGS
     * Settings.ACTION_LOCATION_SOURCE_SETTINGS
     * Settings.ACTION_DISPLAY_SETTINGS
     * Settings.ACTION_SOUND_SETTINGS
     * Settings.ACTION_SECURITY_SETTINGS
     * Settings.ACTION_PRIVACY_SETTINGS
     * Settings.ACTION_APPLICATION_SETTINGS
     * Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
     * Settings.ACTION_APPLICATION_DETAILS_SETTINGS
     * Settings.ACTION_APP_NOTIFICATION_SETTINGS
     */
    fun openSettings(context: Context, settingAction: String) {
        val intent = Intent(settingAction)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent)
    }

    fun openSystemNotification(context: Context, packageName: String) {
        val intent = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                Intent().apply {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }

            }
            else -> {
                Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = Uri.parse("package:$packageName")
                }
            }
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent)
    }

    fun openDetailSetting(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.parse("package:$packageName"))
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent)
    }

    /**
     * open Browser
     */
    fun openBrowsers(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent)
    }

    enum class AndroidShareSheetType(val value: String) {
        TEXT_PLAIN("text/plain"),
        IMAGE_PLAIN("image/*")

    }

    /**
     * TODO : More need to upgrade
     * version : 1.0
     *
     * Support : Text, Image
     */
    fun openShareSheetForChooser(context: Context, shareTitle: String, shareText: String, shareType: AndroidShareSheetType = AndroidShareSheetType.TEXT_PLAIN) {
        val intent = Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND

            putExtra(Intent.EXTRA_TITLE, "$shareTitle")
            putExtra(Intent.EXTRA_TEXT, "$shareText")

            this.type = shareType.value
            when(shareType) {
                // TEXT
                AndroidShareSheetType.TEXT_PLAIN -> {

                }
                // IMAGE
                AndroidShareSheetType.IMAGE_PLAIN -> {

                }
                else ->{

                }
            }
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }, null)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent)
    }
}