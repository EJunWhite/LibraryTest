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
package library.android.support.helper.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import library.android.support.R

class AlertDialogHelper {
    companion object {
        private var instance: AlertDialogHelper? = null

        fun getInstance(): AlertDialogHelper {
            return instance ?: synchronized(this) {
                instance ?: AlertDialogHelper().also { instance = it }
            }
        }
    }

    /**
     * Default Dialog
     */
    fun dialogShow(
        context: Context,
        title: String,
        desc: String,
        positiveTitle: String,
        positiveListener: DialogInterface.OnClickListener,
        negativeTitle: String = "",
        negativeListener: DialogInterface.OnClickListener? = null,
        cancelable: Boolean = true,
    ) {
        val alertDialog: AlertDialog? = context.let {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(desc)
            builder.setPositiveButton(positiveTitle, positiveListener)
            builder.setNegativeButton(negativeTitle, negativeListener)
            builder.setCancelable(cancelable)
            builder.create()
        }
        alertDialog?.show()
    }

    /**
     * Default Dialog
     */
    fun dialogMaterial3Show(
        context: Context,
        title: String,
        desc: String,
        positiveTitle: String,
        positiveListener: DialogInterface.OnClickListener,
        negativeTitle: String = "",
        negativeListener: DialogInterface.OnClickListener? = null,
        cancelable: Boolean = true
    ) {
        val builder = MaterialAlertDialogBuilder(context).apply {
            setTitle(title)
            setMessage(desc)
            setPositiveButton(positiveTitle, positiveListener)
            setNegativeButton(negativeTitle, negativeListener)
            setCancelable(cancelable)
        }

        builder.show()

    }
}
