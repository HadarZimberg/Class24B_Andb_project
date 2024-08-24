package com.example.colorpicker;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String PREF_NAME = "color_picker_prefs";
    private static final String KEY_COLOR_PREFIX = "saved_color_";
    private static final String KEY_COLOR_INDEX = "saved_color_index";
    private SharedPreferences sharedPreferences;

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveColor(int index, int color) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_COLOR_PREFIX + index, color);
        editor.apply();
    }

    public int loadColor(int index, int defaultColor) {
        return sharedPreferences.getInt(KEY_COLOR_PREFIX + index, defaultColor);
    }

    public void saveColorIndex(int index) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_COLOR_INDEX, index);
        editor.apply();
    }

    public int loadColorIndex(int defaultIndex) {
        return sharedPreferences.getInt(KEY_COLOR_INDEX, defaultIndex);
    }
}
