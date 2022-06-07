package com.note.daily.keep.ext

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.note.daily.keep.m.ChecklistItem

fun String.parseChecklistItems(): ArrayList<ChecklistItem>? {
    if (startsWith("[{") && endsWith("}]")) {
        try {
            val checklistItemType = object : TypeToken<List<ChecklistItem>>() {}.type
            return Gson().fromJson<ArrayList<ChecklistItem>>(this, checklistItemType) ?: null
        } catch (e: Exception) {
        }
    }
    return null
}
