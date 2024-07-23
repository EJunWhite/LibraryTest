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

import android.os.Build
import android.util.Log

class DeviceHelper private constructor() {
    companion object {
        private var instance: DeviceHelper? = null

        fun getInstance(): DeviceHelper {
            return instance ?: synchronized(this) {
                instance ?: DeviceHelper().also {
                    instance = it
                }
            }
        }
    }

    fun checkDeviceInformation() {
        Log.d("$TAG", "OS VERSION ===================")
        Log.d("$TAG", "BOARD : ${Build.BOARD}")
        Log.d("$TAG", "BRAND ${Build.BRAND}")
        Log.d("$TAG", "DEVICE ${Build.DEVICE}")
        Log.d("$TAG", "HARDWARE ${Build.HARDWARE}")
        Log.d("$TAG", "MODEL ${Build.MODEL}")
        Log.d("$TAG", "VERSION ${Build.DISPLAY}")
        Log.d("$TAG", "ID ${Build.ID}")
        Log.d("$TAG", "HOST ${Build.HOST}")
        Log.d("$TAG", "MANUFACTURER ${Build.MANUFACTURER}")
        Log.d("$TAG", "CODENAME ${Build.VERSION.CODENAME}")
        Log.d("$TAG", "SDK_INT ${Build.VERSION.SDK_INT}")
        Log.d("$TAG", "INCREMENTAL ${Build.VERSION.INCREMENTAL}")
        Log.d("$TAG", "PREVIEW_SDK_INT ${Build.VERSION.PREVIEW_SDK_INT}")
        Log.d("$TAG", "RELEASE ${Build.VERSION.RELEASE}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.d("$TAG", "RELEASE_OR_CODENAME ${Build.VERSION.RELEASE_OR_CODENAME}")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d("$TAG", "MEDIA_PERFORMANCE_CLASS ${Build.VERSION.MEDIA_PERFORMANCE_CLASS}")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("$TAG", "RELEASE_OR_PREVIEW_DISPLAY ${Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY}")
        }
        Log.d("$TAG", "SECURITY_PATCH ${Build.VERSION.SECURITY_PATCH}")
        Log.d("$TAG", "OS VERSION ===================")
    }
}
