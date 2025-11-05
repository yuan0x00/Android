package com.rapid.android.core.storage;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 搜索历史存储
 */
public final class SearchHistoryStorage {

    private static final String PREF_NAME = "search_history";
    private static final String KEY_HISTORY = "history_list";
    private static final int MAX_SIZE = 10;

    private SearchHistoryStorage() {
    }

    private static PreferenceHelper getPrefs() {
        return PreferenceHelper.getDefault();
    }

    public static List<String> getHistories() {
        String stored = getPrefs().getString(KEY_HISTORY, "");
        if (TextUtils.isEmpty(stored)) {
            return new ArrayList<>();
        }

        // 使用简单的分隔符存储，避免 JSON 序列化开销
        String[] items = stored.split("\\|\\|");
        List<String> result = new ArrayList<>();
        for (String item : items) {
            if (!TextUtils.isEmpty(item)) {
                result.add(item);
            }
        }
        return result;
    }

    public static void addHistory(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            return;
        }

        List<String> histories = getHistories();
        Set<String> ordered = new LinkedHashSet<>();

        // 新搜索放在最前面
        ordered.add(keyword);

        // 添加已有历史（去重）
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
        getPrefs().remove(KEY_HISTORY);
    }

    private static void save(List<String> histories) {
        if (histories == null || histories.isEmpty()) {
            getPrefs().remove(KEY_HISTORY);
            return;
        }

        // 限制最大数量
        int size = Math.min(histories.size(), MAX_SIZE);
        List<String> limited = histories.subList(0, size);

        // 使用 || 分隔符连接（确保不与搜索关键词冲突）
        String joined = TextUtils.join("||", limited);
        getPrefs().putString(KEY_HISTORY, joined);
    }
}
