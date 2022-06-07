package com.note.daily.keep.extensions

import androidx.fragment.app.Fragment
import com.note.daily.keep.helpers.Config

val Fragment.config: Config? get() = if (context != null) Config.newInstance(context!!) else null
