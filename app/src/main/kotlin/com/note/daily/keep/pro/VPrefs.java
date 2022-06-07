package com.note.daily.keep.pro;

import android.content.Context;

public class VPrefs extends VData {

    private static final String K_LOCAL_PURCHASED = "local_purchased";
    private static final String K_LOCAL_SUBSCRIBED = "local_subscribed";

    private static VPrefs instance;

    private VPrefs(Context context) {
        super(context, "snotes");
    }

    public static void init(Context context) {
        instance = new VPrefs(context);
    }

    public static VPrefs get() {
        if (instance == null) {
            throw new NullPointerException("Initialization require!");
        }
        return instance;
    }

    public void updateLocalPurchasedState(boolean purchased) {
        putBoolean(K_LOCAL_PURCHASED, purchased);
    }

    public boolean isLocalPurchasedState() {
        return getBoolean(K_LOCAL_PURCHASED, false);
    }

    public void updateLocalSubscribedState(boolean purchased) {
        putBoolean(K_LOCAL_SUBSCRIBED, purchased);
    }

    public boolean isLocalSubscribedState() {
        return getBoolean(K_LOCAL_SUBSCRIBED, false);
    }

}
