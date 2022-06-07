package com.note.daily.keep.h

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.simplemobiletools.commons.helpers.PROTECTION_NONE
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.note.daily.keep.R
import com.note.daily.keep.ext.config
import com.note.daily.keep.ext.notesDB
import com.note.daily.keep.m.Note
import java.io.File

class NotesHelper(val context: Context) {
    fun getNotes(callback: (notes: ArrayList<Note>) -> Unit) {
        ensureBackgroundThread {
            // make sure the initial note has enough time to be precreated
            if (context.config.appRunCount <= 1) {
                context.notesDB.getNotes()
                Thread.sleep(200)
            }

            val notes = context.notesDB.getNotes() as ArrayList<Note>
            val notesToDelete = ArrayList<Note>(notes.size)
            notes.forEach {
                if (it.path.isNotEmpty()) {
                    if (!it.path.startsWith("content://") && !File(it.path).exists()) {
                        context.notesDB.deleteNote(it)
                        notesToDelete.add(it)
                    }
                }
            }

            notes.removeAll(notesToDelete)

            if (notes.isEmpty()) {
                val generalNote = context.resources.getString(R.string.my_notes)
                val note = Note(null, generalNote, "", NoteType.TYPE_TEXT.value, "", PROTECTION_NONE, "")
                context.notesDB.insertOrUpdate(note)
                notes.add(note)
            }

            Handler(Looper.getMainLooper()).post {
                callback(notes)
            }
        }
    }

    fun getNoteWithId(id: Long, callback: (note: Note?) -> Unit) {
        ensureBackgroundThread {
            val note = context.notesDB.getNoteWithId(id)
            Handler(Looper.getMainLooper()).post {
                callback(note)
            }
        }
    }

    fun getNoteIdWithPath(path: String, callback: (id: Long?) -> Unit) {
        ensureBackgroundThread {
            val id = context.notesDB.getNoteIdWithPath(path)
            Handler(Looper.getMainLooper()).post {
                callback(id)
            }
        }
    }

    fun insertOrUpdateNote(note: Note, callback: ((newNoteId: Long) -> Unit)? = null) {
        ensureBackgroundThread {
            val noteId = context.notesDB.insertOrUpdate(note)
            Handler(Looper.getMainLooper()).post {
                callback?.invoke(noteId)
            }
        }
    }
}