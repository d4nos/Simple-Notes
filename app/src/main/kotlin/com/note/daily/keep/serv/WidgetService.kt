package com.note.daily.keep.serv

import android.content.Intent
import android.widget.RemoteViewsService
import com.note.daily.keep.adap.WidgetAdapter

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = WidgetAdapter(applicationContext, intent)
}
