package com.note.daily.keep.dg

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.PROTECTION_NONE
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.note.daily.keep.R
import com.note.daily.keep.ext.config
import com.note.daily.keep.ext.notesDB
import com.note.daily.keep.h.NoteType
import com.note.daily.keep.m.Note
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.value
import kotlinx.android.synthetic.main.dialog_new_note.view.*

class NewNoteDialog(val activity: Activity, title: String? = null, val setChecklistAsDefault: Boolean, callback: (note: Note) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_new_note, null).apply {
            val defaultType = when {
                setChecklistAsDefault -> type_checklist.id
                activity.config.lastCreatedNoteType == NoteType.TYPE_TEXT.value -> type_text_note.id
                else -> type_checklist.id
            }

            new_note_type.check(defaultType)
        }

        view.note_title.setText(title)

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.new_note) {
                    showKeyboard(view.note_title)
                    getButton(BUTTON_POSITIVE).setOnClickListener {
                        val title = view.note_title.value
                        ensureBackgroundThread {
                            when {
                                title.isEmpty() -> activity.toast(R.string.no_title)
                                activity.notesDB.getNoteIdWithTitle(title) != null -> activity.toast(R.string.title_taken)
                                else -> {
                                    val type =
                                        if (view.new_note_type.checkedRadioButtonId == view.type_checklist.id) NoteType.TYPE_CHECKLIST.value else NoteType.TYPE_TEXT.value
                                    activity.config.lastCreatedNoteType = type
                                    val newNote = Note(null, title, "", type, "", PROTECTION_NONE, "")
                                    callback(newNote)
                                    dismiss()
                                }
                            }
                        }
                    }
                }
            }
    }
}
