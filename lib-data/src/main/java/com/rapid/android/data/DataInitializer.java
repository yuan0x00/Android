package com.rapid.android.data;

import com.core.datastore.DefaultDataStore;
import com.core.datastore.IDataStore;
import com.rapid.android.data.local.AuthStorage;

public class DataInitializer {
    
    public static void init() {
        // 初始化AuthStorage
        IDataStore dataStore = new DefaultDataStore();
        AuthStorage.init(dataStore);
    }
}