package com.rapid.android.core.data;

import com.rapid.android.core.data.local.AuthStorage;
import com.rapid.android.core.datastore.DefaultDataStore;
import com.rapid.android.core.datastore.IDataStore;

public class DataInitializer {
    
    public static void init() {
        // 初始化AuthStorage
        IDataStore dataStore = new DefaultDataStore();
        AuthStorage.init(dataStore);
    }
}