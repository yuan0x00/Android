package com.rapid.android.core.analytics.tracker;

import java.util.Map;

public class Event {
    private String name;
    private long timestamp;
    private Map<String, Object> params;

    public Event(String name, Map<String, Object> params) {
        this.name = name;
        this.timestamp = System.currentTimeMillis();
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                ", timestamp=" + timestamp +
                ", params=" + params +
                '}';
    }
}
