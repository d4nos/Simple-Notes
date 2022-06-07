package com.note.daily.keep.dg

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.*
import com.note.daily.keep.R
import kotlinx.android.synthetic.main.dialog_new_checklist_item.view.*
import kotlinx.android.synthetic.main.item_add_checklist.view.*

class NewChecklistItemDialog(val activity: Activity, callback: (titles: ArrayList<String>) -> Unit) {
    private val titles = mutableListOf<EditText>()
    private val textColor = activity.getProperTextColor()
    private val view: ViewGroup = activity.layoutInflater.inflate(R.layout.dialog_new_checklist_item, null) as ViewGroup

    init {
        addNewEditText()
        view.apply {
            add_item.applyColorFilter(activity.getProperPrimaryColor())
            add_item.background.applyColorFilter(textColor)
            add_item.setOnClickListener {
                addNewEditText()
            }
        }

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.add_new_checklist_items) {
                    showKeyboard(titles.first())
                    getButton(BUTTON_POSITIVE).setOnClickListener {
                        when {
                            titles.all { it.text.isEmpty() } -> activity.toast(R.string.empty_name)
                            else -> {
                                val titles = titles.map { it.text.toString() }.filter { it.isNotEmpty() }.toMutableList() as ArrayList<String>
                                callback(titles)
                                dismiss()
                            }
                        }
                    }
                }
            }
    }

    private fun addNewEditText() {
        activity.layoutInflater.inflate(R.layout.item_add_checklist, null).apply {
            title_edit_text.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || actionId == KeyEvent.KEYCODE_ENTER) {
                    addNewEditText()
                    true
                } else {
                    false
                }
            }
            titles.add(title_edit_text)
            view.checklist_holder.addView(this)
            activity.updateTextColors(view.checklist_holder)
            view.dialog_holder.post {
                view.dialog_holder.fullScroll(View.FOCUS_DOWN)
                activity.showKeyboard(title_edit_text)
            }
        }
    }
}
