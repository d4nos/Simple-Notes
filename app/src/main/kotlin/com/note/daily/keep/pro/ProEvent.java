package com.note.daily.keep.pro;


import org.greenrobot.eventbus.EventBus;

public class ProEvent {
    public static void sendEvent() {
        EventBus.getDefault().post(new ProEvent());
    }
}
