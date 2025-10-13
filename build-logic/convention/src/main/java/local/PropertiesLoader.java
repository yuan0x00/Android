package local;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public final class PropertiesLoader {

    private static final Map<String, CachedProperties> CACHE = new ConcurrentHashMap<>();

    private PropertiesLoader() {
    }

    public static Properties loadPropertiesFile(Project project, String fileName) {
        File configFile = project.getRootDir().toPath().resolve(fileName).toFile();
        if (!configFile.exists()) {
            throw new GradleException("Not found at: " + configFile.getAbsolutePath());
        }

        String cacheKey = configFile.getAbsolutePath();
        long lastModified = configFile.lastModified();

        CachedProperties cached = CACHE.get(cacheKey);
        if (cached != null && cached.lastModified == lastModified) {
            return copyOf(cached.properties);
        }

        Properties loaded = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            loaded.load(fis);
        } catch (IOException e) {
            throw new GradleException("Failed to read", e);
        }

        CACHE.put(cacheKey, new CachedProperties(lastModified, copyOf(loaded)));
        return copyOf(loaded);
    }

    /**
     * 从 Properties 中获取String值。
     */
    public static String getPropertyString(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value;
    }

    /**
     * 从 Properties 中获取整数值。
     */
    public static int getPropertyInt(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid integer value for '" + key + "': " + value, e);
        }
    }

    /**
     * 从 Properties 中获取布尔值，支持默认值。
     */
    public static boolean getPropertyBoolean(Properties props, String key, boolean defaultValue) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private static Properties copyOf(Properties source) {
        Properties copy = new Properties();
        copy.putAll(source);
        return copy;
    }

    private record CachedProperties(long lastModified, Properties properties) {
    }
}
