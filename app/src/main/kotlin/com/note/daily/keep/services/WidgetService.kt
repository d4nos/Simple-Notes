package com.note.daily.keep.services

import android.content.Intent
import android.widget.RemoteViewsService
import com.note.daily.keep.adapters.WidgetAdapter

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = WidgetAdapter(applicationContext, intent)
}
