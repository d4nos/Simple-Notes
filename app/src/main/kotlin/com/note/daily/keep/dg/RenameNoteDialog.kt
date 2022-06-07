package com.note.daily.keep.dg

import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.note.daily.keep.R
import com.note.daily.keep.act.SimpleActivity
import com.note.daily.keep.ext.config
import com.note.daily.keep.ext.notesDB
import com.note.daily.keep.h.NotesHelper
import com.note.daily.keep.m.Note
import kotlinx.android.synthetic.main.dialog_new_note.view.*
import java.io.File

class RenameNoteDialog(val activity: SimpleActivity, val note: Note, val currentNoteText: String?, val callback: (note: Note) -> Unit) {

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_rename_note, null)
        view.note_title.setText(note.title)

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.rename_note) {
                    showKeyboard(view.note_title)
                    getButton(BUTTON_POSITIVE).setOnClickListener {
                        val title = view.note_title.value
                        ensureBackgroundThread {
                            newTitleConfirmed(title, this)
                        }
                    }
                }
            }
    }

    private fun newTitleConfirmed(title: String, dialog: AlertDialog) {
        when {
            title.isEmpty() -> activity.toast(R.string.no_title)
            activity.notesDB.getNoteIdWithTitleCaseSensitive(title) != null -> activity.toast(R.string.title_taken)
            else -> {
                note.title = title
                if (activity.config.autosaveNotes && currentNoteText != null) {
                    note.value = currentNoteText
                }

                val path = note.path
                if (path.isEmpty()) {
                    activity.notesDB.insertOrUpdate(note)
                    activity.runOnUiThread {
                        dialog.dismiss()
                        callback(note)
                    }
                } else {
                    if (title.isEmpty()) {
                        activity.toast(R.string.filename_cannot_be_empty)
                        return
                    }

                    val file = File(path)
                    val newFile = File(file.parent, title)
                    if (!newFile.name.isAValidFilename()) {
                        activity.toast(R.string.invalid_name)
                        return
                    }

                    activity.renameFile(file.absolutePath, newFile.absolutePath, false) { success, useAndroid30Way ->
                        if (success) {
                            note.path = newFile.absolutePath
                            NotesHelper(activity).insertOrUpdateNote(note) {
                                dialog.dismiss()
                                callback(note)
                            }
                        } else {
                            activity.toast(R.string.rename_file_error)
                            return@renameFile
                        }
                    }
                }

            }
        }
    }
}
