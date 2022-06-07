package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.text.Html
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.humanizePath
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_write_permission.view.*
import kotlinx.android.synthetic.main.dialog_write_permission_otg.view.*

class WritePermissionDialog(activity: Activity, val mode: Mode, val callback: () -> Unit) {
    sealed class Mode {
        object Otg : Mode()
        object SdCard : Mode()
        data class OpenDocumentTreeSDK30(val path: String) : Mode()
        object CreateDocumentSDK30 : Mode()
    }

    var dialog: AlertDialog

    init {
        val layout = if (mode == Mode.SdCard) R.layout.dialog_write_permission else R.layout.dialog_write_permission_otg
        val view = activity.layoutInflater.inflate(layout, null)

        when (mode) {
            Mode.Otg -> {
                view.write_permissions_dialog_otg_text.setText(R.string.confirm_usb_storage_access_text)
            }
            Mode.SdCard -> {
            }
            is Mode.OpenDocumentTreeSDK30 -> {
                val humanizedPath = activity.humanizePath(mode.path)
                view.write_permissions_dialog_otg_text.text =
                    Html.fromHtml(activity.getString(R.string.confirm_storage_access_android_text_specific, humanizedPath))

                view.write_permissions_dialog_otg_image.setOnClickListener {
                    dialogConfirmed()
                }
            }
            Mode.CreateDocumentSDK30 -> {
                view.write_permissions_dialog_otg_text.text = Html.fromHtml(activity.getString(R.string.confirm_create_doc_for_new_folder_text))

                view.write_permissions_dialog_otg_image.setOnClickListener {
                    dialogConfirmed()
                }
            }
        }

        dialog = AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
            .setOnCancelListener {
                BaseSimpleActivity.funAfterSAFPermission?.invoke(false)
                BaseSimpleActivity.funAfterSAFPermission = null
            }
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.confirm_storage_access_title)
            }
    }

    private fun dialogConfirmed() {
        dialog.dismiss()
        callback()
    }
}
