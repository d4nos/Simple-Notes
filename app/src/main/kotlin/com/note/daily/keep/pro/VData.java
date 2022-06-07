package com.note.daily.keep.pro;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public abstract class VData {
    protected final Context context;
    private final SharedPreferences pref;

    protected VData(Context context, String name) {
        this.context = context;
        this.pref = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public SharedPreferences pref() {
        return pref;
    }

    public Context getContext() {
        return context;
    }

    public void putString(final String key, final String newValue) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, newValue);
        editor.apply();
    }

    public String getString(final String key, final String defValue) {
        return pref.getString(key, defValue);
    }

    public void putStringSet(final String key, final Set<String> value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet(key, value);
        editor.apply();
    }

    public Set<String> getStringSet(final String key, final Set<String> defValue) {
        return pref.getStringSet(key, defValue);
    }

    public void putInt(final String key, final int newValue) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, newValue);
        editor.apply();
    }

    public int getInt(final String key, final int defValue) {
        return pref.getInt(key, defValue);
    }

    public void putFloat(final String key, final float newValue) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat(key, newValue);
        editor.apply();
    }

    public float getFloat(final String key, final float defValue) {
        return pref.getFloat(key, defValue);
    }

    public void putLong(final String key, final long newValue) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, newValue);
        editor.apply();
    }

    public long getLong(final String key, final long defValue) {
        return pref.getLong(key, defValue);
    }

    public void putBoolean(final String key, final Boolean newValue) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, newValue);
        editor.apply();
    }

    public boolean getBoolean(final String key, final Boolean defValue) {
        return pref.getBoolean(key, defValue);
    }

    public void put(String key, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof Boolean) {
            putBoolean(key, (Boolean) value);
            return;
        }
        if (value instanceof String) {
            putString(key, (String) value);
            return;
        }
        if (value instanceof Integer) {
            putInt(key, (Integer) value);
            return;
        }
        if (value instanceof Long) {
            putLong(key, (Long) value);
            return;
        }
        if (value instanceof Float) {
            putFloat(key, (Float) value);
        }
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key);
        editor.apply();
    }
}
