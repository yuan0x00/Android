package com.rapid.android.network.proxy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ProxySettings {

    private final boolean enabled;
    private final String host;
    private final int port;
    private final boolean autoDisableOnFailure;
    private final long lastFailureTimestamp;
    private final String lastFailureReason;

    private ProxySettings(Builder builder) {
        this.enabled = builder.enabled && isHostValid(builder.host) && isPortValid(builder.port);
        this.host = builder.host != null ? builder.host : "";
        this.port = builder.port;
        this.autoDisableOnFailure = builder.autoDisableOnFailure;
        this.lastFailureTimestamp = builder.lastFailureTimestamp;
        this.lastFailureReason = builder.lastFailureReason;
    }

    @NonNull
    public static ProxySettings disabled() {
        return builder().enabled(false).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static boolean isHostValid(@Nullable String value) {
        return value != null && value.trim().length() > 0;
    }

    private static boolean isPortValid(int value) {
        return value > 0 && value <= 65535;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @NonNull
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isAutoDisableOnFailure() {
        return autoDisableOnFailure;
    }

    public long getLastFailureTimestamp() {
        return lastFailureTimestamp;
    }

    @Nullable
    public String getLastFailureReason() {
        return lastFailureReason;
    }

    public boolean isConfigured() {
        return enabled && isHostValid(host) && isPortValid(port);
    }

    public Builder toBuilder() {
        return builder()
                .enabled(enabled)
                .host(host)
                .port(port)
                .autoDisableOnFailure(autoDisableOnFailure)
                .lastFailure(lastFailureTimestamp, lastFailureReason);
    }

    public static final class Builder {
        private boolean enabled;
        private String host = "";
        private int port;
        private boolean autoDisableOnFailure = true;
        private long lastFailureTimestamp;
        private String lastFailureReason;

        private Builder() {
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder host(@Nullable String host) {
            this.host = host != null ? host.trim() : "";
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder autoDisableOnFailure(boolean autoDisableOnFailure) {
            this.autoDisableOnFailure = autoDisableOnFailure;
            return this;
        }

        public Builder lastFailure(long timestamp, @Nullable String reason) {
            this.lastFailureTimestamp = timestamp;
            this.lastFailureReason = reason;
            return this;
        }

        public ProxySettings build() {
            return new ProxySettings(this);
        }
    }
}
