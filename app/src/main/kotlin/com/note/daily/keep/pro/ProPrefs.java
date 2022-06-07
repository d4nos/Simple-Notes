package com.note.daily.keep.pro;

import android.content.Context;

public class ProPrefs extends ProData {

    private static final String K_LOCAL_PURCHASED = "local_purchased";
    private static final String K_LOCAL_SUBSCRIBED = "local_subscribed";

    private static ProPrefs instance;

    private ProPrefs(Context context) {
        super(context, "notes");
    }

    public static void init(Context context) {
        instance = new ProPrefs(context);
    }

    public static ProPrefs get() {
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
