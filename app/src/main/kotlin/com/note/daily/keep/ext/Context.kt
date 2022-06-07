package com.note.daily.keep.ext

import android.content.Context
import com.note.daily.keep.R
import com.note.daily.keep.db.NotesDatabase
import com.note.daily.keep.h.Config
import com.note.daily.keep.itfc.NotesDao
import com.note.daily.keep.itfc.WidgetsDao

val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.notesDB: NotesDao get() = NotesDatabase.getInstance(applicationContext).NotesDao()

val Context.widgetsDB: WidgetsDao get() = NotesDatabase.getInstance(applicationContext).WidgetsDao()

fun Context.getPercentageFontSize() = resources.getDimension(R.dimen.middle_text_size) * (config.fontSizePercentage / 100f)
