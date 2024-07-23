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

class AndroidSupportHelper private constructor() {
    companion object {
        private var instance: AndroidSupportHelper? = null

        fun getInstance(): AndroidSupportHelper {
            return instance ?: synchronized(this) {
                instance ?: AndroidSupportHelper().also { instance = it }
            }
        }
    }

    fun checkApplication() {
        // get Information
        DeviceHelper.getInstance().checkDeviceInformation()

        // check permission

        // check Bluetooth

        // check network

        // check Location

        // check sensor

        // check notification

        // check storage

        // check battery

        // check memory

        // check cpu

        // check screen


    }
}
