package local;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class PropertiesLoader {

    private PropertiesLoader() {
    }

    public static Properties loadPropertiesFile(Project project, String fileName) {
        File configFile = project.getRootDir().toPath().resolve(fileName).toFile();
        if (!configFile.exists()) {
            throw new GradleException("Not found at: " + configFile.getAbsolutePath());
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            props.load(fis);
        } catch (IOException e) {
            throw new GradleException("Failed to read", e);
        }
        return props;
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
}
