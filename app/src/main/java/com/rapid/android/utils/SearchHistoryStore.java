package com.rapid.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.rapid.android.core.common.app.BaseApplication;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SearchHistoryStore {

    private static final String PREF_NAME = "search_history";
    private static final String KEY_HISTORY = "key_history";
    private static final int MAX_SIZE = 10;

    private SearchHistoryStore() {
    }

    private static SharedPreferences getPrefs() {
        Context context = BaseApplication.getAppContext();
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static List<String> getHistories() {
        String json = getPrefs().getString(KEY_HISTORY, "");
        List<String> result = new ArrayList<>();
        if (TextUtils.isEmpty(json)) {
            return result;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                String value = array.optString(i, "");
                if (!TextUtils.isEmpty(value)) {
                    result.add(value);
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public static void addHistory(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            return;
        }
        List<String> histories = getHistories();
        Set<String> ordered = new LinkedHashSet<>();
        ordered.add(keyword);
        for (String item : histories) {
            if (!TextUtils.isEmpty(item) && !item.equalsIgnoreCase(keyword)) {
                ordered.add(item);
            }
            if (ordered.size() >= MAX_SIZE) {
                break;
            }
        }
        save(new ArrayList<>(ordered));
    }

    public static void clearHistory() {
        getPrefs().edit().remove(KEY_HISTORY).apply();
    }

    private static void save(List<String> histories) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < histories.size() && i < MAX_SIZE; i++) {
            array.put(histories.get(i));
        }
        getPrefs().edit().putString(KEY_HISTORY, array.toString()).apply();
    }
}
