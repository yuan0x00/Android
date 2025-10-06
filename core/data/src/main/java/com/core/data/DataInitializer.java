package com.core.data;

import com.core.data.local.AuthStorage;
import com.core.datastore.DefaultDataStore;
import com.core.datastore.IDataStore;

public class DataInitializer {
    
    public static void init() {
        // 初始化AuthStorage
        IDataStore dataStore = new DefaultDataStore();
        AuthStorage.init(dataStore);
    }
}