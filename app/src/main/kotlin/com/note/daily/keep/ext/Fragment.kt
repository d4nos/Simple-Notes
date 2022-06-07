package com.note.daily.keep.ext

import androidx.fragment.app.Fragment
import com.note.daily.keep.h.Config

val Fragment.config: Config? get() = if (context != null) Config.newInstance(context!!) else null
