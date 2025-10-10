package com.rapid.android.core.storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU 内存缓存管理器
 * 基于 LinkedHashMap 实现 LRU 算法
 *
 * @param <K> Key 类型
 * @param <V> Value 类型
 */
public class CacheManager<K, V> {

    private final LinkedHashMap<K, V> cache;
    private final int maxSize;

    public CacheManager(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be positive");
        }
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<K, V>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > CacheManager.this.maxSize;
            }
        };
    }

    /**
     * 放入缓存
     */
    public synchronized void put(@NonNull K key, @NonNull V value) {
        cache.put(key, value);
    }

    /**
     * 获取缓存
     */
    @Nullable
    public synchronized V get(@NonNull K key) {
        return cache.get(key);
    }

    /**
     * 移除缓存
     */
    @Nullable
    public synchronized V remove(@NonNull K key) {
        return cache.remove(key);
    }

    /**
     * 是否包含
     */
    public synchronized boolean contains(@NonNull K key) {
        return cache.containsKey(key);
    }

    /**
     * 清空缓存
     */
    public synchronized void clear() {
        cache.clear();
    }

    /**
     * 缓存大小
     */
    public synchronized int size() {
        return cache.size();
    }

    /**
     * 最大容量
     */
    public int maxSize() {
        return maxSize;
    }
}
