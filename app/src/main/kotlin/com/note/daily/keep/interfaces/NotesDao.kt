package com.note.daily.keep.interfaces

import androidx.room.*
import com.note.daily.keep.models.Note

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes ORDER BY title COLLATE NOCASE ASC")
    fun getNotes(): List<Note>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteWithId(id: Long): Note?

    @Query("SELECT id FROM notes WHERE path = :path")
    fun getNoteIdWithPath(path: String): Long?

    @Query("SELECT id FROM notes WHERE title = :title COLLATE NOCASE")
    fun getNoteIdWithTitle(title: String): Long?

    @Query("SELECT id FROM notes WHERE title = :title")
    fun getNoteIdWithTitleCaseSensitive(title: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(note: Note): Long

    @Delete
    fun deleteNote(note: Note)
}
