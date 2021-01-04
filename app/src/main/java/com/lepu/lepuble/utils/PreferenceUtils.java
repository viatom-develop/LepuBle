package com.lepu.lepuble.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;

public class PreferenceUtils {

    private final static String PREFERENCE_TAG = "lepu";

    /**
     * 保存本地Long设置
     * @param key
     * @param value
     */
    public static void savePreferences(@Nullable Context context, @Nullable String key, long value) {
        if (key == null || context == null) {
            return;
        }
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = context.getSharedPreferences(PREFERENCE_TAG, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * 保存本地Float设置
     * @param key
     * @param value
     */
    public static void savePreferences(@Nullable Context context, @Nullable String key, float value) {
        if (key == null || context == null) {
            return;
        }
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = context.getSharedPreferences(PREFERENCE_TAG, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    /**
     * 保存本地Int设置
     * @param key
     * @param value
     */
    public static void savePreferences(@Nullable Context context, @Nullable String key, int value) {
        if (key == null || context == null) {
            return;
        }
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = context.getSharedPreferences(PREFERENCE_TAG, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * 保存本地String设置
     * @param key
     * @param value
     */
    public static void savePreferences(@Nullable Context context, @Nullable String key, @Nullable String value) {
        if (key == null || context == null || value == null) {
            return;
        }
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = context.getSharedPreferences(PREFERENCE_TAG, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 保存本地bool设置
     * @param key
     * @param value
     */
    public static void savePreferences(@Nullable Context context, @Nullable String key, @Nullable Boolean value) {
        if (key == null || context == null || value == null) {
            return;
        }
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = context.getSharedPreferences(PREFERENCE_TAG, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * 读取本地long设置
     * @param key
     * @return
     */
    public static long readLongPreferences(@Nullable Context context, @Nullable String key) {
        if (key == null || context == null) {
            return 0;
        }
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(PREFERENCE_TAG, Context.MODE_PRIVATE);

        return preferences.getLong(key, 0);
    }

    /**
     * 读取本地float设置
     * @param key
     * @return
     */
    public static float readFloatPreferences(@Nullable Context context, @Nullable String key) {
        if (key == null || context == null) {
            return 0;
        }
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(PREFERENCE_TAG, Context.MODE_PRIVATE);

        return preferences.getFloat(key, 0);
    }

    /**
     * 读取本地Int设置
     * @param key
     * @return
     */
    public static int readIntPreferences(@Nullable Context context, @Nullable String key) {
        if (key == null || context == null) {
            return 0;
        }
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(PREFERENCE_TAG, Context.MODE_PRIVATE);

        return preferences.getInt(key, 0);
    }

    /**
     * 读取本地Bool设置
     * @param key
     * @return
     */
    public static boolean readBoolPreferences(@Nullable Context context, @Nullable String key) {
        if (key == null || context == null) {
            return false;
        }
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(PREFERENCE_TAG, Context.MODE_PRIVATE);

        return preferences.getBoolean(key, false);
    }

    /**
     * 读取本地String设置
     * @param key
     * @return
     */
    @Nullable
    public static String readStrPreferences(@Nullable Context context, @Nullable String key) {
        if (key == null || context == null) {
            return null;
        }
        SharedPreferences preferences;
        preferences = context.getSharedPreferences(PREFERENCE_TAG, Context.MODE_PRIVATE);

        return preferences.getString(key, null);
    }

    /**
     * 删除所有设置
     * @param context
     */
    public static void removeAllPreferences(@Nullable Context context) {
        if (context == null) {
            return;
        }
        LogUtils.d("删除所有设置");
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = context.getSharedPreferences(PREFERENCE_TAG, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
    public static void removeStrPreferences(@Nullable Context context, @Nullable String key) {
        if (key == null ||context == null) {
            return;
        }
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences = context.getSharedPreferences(PREFERENCE_TAG, Context.MODE_PRIVATE);
        if (preferences.getString(key,null)!=null){
            editor = preferences.edit();
            editor.putString(key, null);
            editor.apply();
        }
    }
}
