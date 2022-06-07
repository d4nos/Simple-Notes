package com.note.daily.keep.dg

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.note.daily.keep.R
import com.simplemobiletools.commons.extensions.*
import com.note.daily.keep.ext.config
import com.note.daily.keep.h.NotesHelper
import com.note.daily.keep.m.Note
import kotlinx.android.synthetic.main.dialog_open_note.view.*
import kotlinx.android.synthetic.main.open_note_item.view.*

class OpenNoteDialog(val activity: Activity, val callback: (checkedId: Long, newNote: Note?) -> Unit) {
    private var dialog: AlertDialog? = null

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_open_note, null)
        NotesHelper(activity).getNotes {
            initDialog(it, view)
        }

        view.dialog_open_note_new_radio.setOnClickListener {
            view.dialog_open_note_new_radio.isChecked = false
            NewNoteDialog(activity, setChecklistAsDefault = false) {
                callback(0, it)
                dialog?.dismiss()
            }
        }
    }

    private fun initDialog(notes: ArrayList<Note>, view: View) {
        val textColor = activity.getProperTextColor()
        notes.forEach {
            activity.layoutInflater.inflate(R.layout.open_note_item, null).apply {
                val note = it
                open_note_item_radio_button.apply {
                    text = note.title
                    isChecked = note.id == activity.config.currentNoteId
                    id = note.id!!.toInt()

                    setOnClickListener {
                        callback(note.id!!, null)
                        dialog?.dismiss()
                    }
                }
                open_note_item_icon.apply {
                    beVisibleIf(note.path.isNotEmpty())
                    applyColorFilter(textColor)
                    setOnClickListener {
                        activity.toast(note.path)
                    }
                }
                view.dialog_open_note_linear.addView(this, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
        }

        dialog = AlertDialog.Builder(activity)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.open_note)
            }
    }
}
