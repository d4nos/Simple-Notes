package com.note.daily.keep.f

import android.util.TypedValue
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.PROTECTION_NONE
import com.note.daily.keep.act.MainActivity
import com.note.daily.keep.ext.config
import com.note.daily.keep.ext.getPercentageFontSize
import com.note.daily.keep.h.NotesHelper
import com.note.daily.keep.m.Note
import kotlinx.android.synthetic.main.fragment_checklist.view.*

abstract class NoteFragment : Fragment() {
    protected var note: Note? = null
    var shouldShowLockedContent = false

    protected fun setupLockedViews(view: ViewGroup, note: Note) {
        view.apply {
            note_locked_layout.beVisibleIf(note.isLocked() && !shouldShowLockedContent)
            note_locked_image.applyColorFilter(requireContext().getProperTextColor())

            note_locked_label.setTextColor(requireContext().getProperTextColor())
            note_locked_label.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getPercentageFontSize())

            note_locked_show.setTextColor(requireContext().getProperPrimaryColor())
            note_locked_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getPercentageFontSize())
            note_locked_show.setOnClickListener {
                handleUnlocking()
            }
        }
    }

    protected fun saveNoteValue(note: Note, content: String?) {
        if (note.path.isEmpty()) {
            NotesHelper(requireActivity()).insertOrUpdateNote(note) {
                (activity as? MainActivity)?.noteSavedSuccessfully(note.title)
            }
        } else {
            if (content != null) {
                val displaySuccess = activity?.config?.displaySuccess ?: false
                (activity as? MainActivity)?.tryExportNoteValueToFile(note.path, note.title, content, displaySuccess)
            }
        }
    }

    fun handleUnlocking(callback: (() -> Unit)? = null) {
        if (callback != null && (note!!.protectionType == PROTECTION_NONE || shouldShowLockedContent)) {
            callback()
            return
        }

        activity?.performSecurityCheck(
            protectionType = note!!.protectionType,
            requiredHash = note!!.protectionHash,
            successCallback = { _, _ ->
                shouldShowLockedContent = true
                checkLockState()
                callback?.invoke()
            }
        )
    }

    fun updateNoteValue(value: String) {
        note?.value = value
    }

    fun updateNotePath(path: String) {
        note?.path = path
    }

    abstract fun checkLockState()
}
