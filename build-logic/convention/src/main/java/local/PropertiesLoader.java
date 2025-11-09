package local;

import org.gradle.api.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    public static final String CONFIG_PROPERTIES = "config.properties";

    public static Properties loadPropertiesFile(Project project, String fileName) {
        Properties properties = new Properties();
        File propertiesFile = project.getRootProject().file(fileName);

        if (propertiesFile.exists()) {
            try (InputStream input = new FileInputStream(propertiesFile)) {
                properties.load(input);
                project.getLogger().info("Loaded properties from: {}", propertiesFile.getAbsolutePath());
            } catch (Exception e) {
                project.getLogger().warn("Failed to load properties from: {}", propertiesFile.getAbsolutePath(), e);
            }
        } else {
            project.getLogger().warn("Properties file not found: {}", propertiesFile.getAbsolutePath());
        }

        return properties;
    }

    public static int getPropertyInt(Properties properties, String key) {
        return getPropertyInt(properties, key, 0);
    }

    public static int getPropertyInt(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                // 使用默认值
            }
        }
        return defaultValue;
    }

    public static String getPropertyString(Properties properties, String key) {
        return getPropertyString(properties, key, null);
    }

    public static String getPropertyString(Properties properties, String key, String defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? value.trim() : defaultValue;
    }

    public static boolean getPropertyBoolean(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value.trim());
        }
        return defaultValue;
    }
}