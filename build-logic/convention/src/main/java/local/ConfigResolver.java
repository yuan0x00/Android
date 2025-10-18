package local;

import org.gradle.api.Project;

import java.io.File;

/**
 * 负责根据环境变量或 Gradle 属性解析配置文件名称。
 */
public final class ConfigResolver {

    private static final String DEFAULT_FILE = "config.properties";

    private ConfigResolver() {
    }

    public static String resolveConfigFile(Project project) {
        String env = findEnvironment(project);
        if (env == null || env.isBlank()) {
            return DEFAULT_FILE;
        }

        String candidate = String.format("config-%s.properties", env);
        File resolved = project.getRootDir().toPath().resolve(candidate).toFile();
        if (resolved.exists()) {
            return candidate;
        }
        return DEFAULT_FILE;
    }

    private static String findEnvironment(Project project) {
        Object prop = project.findProperty("configEnv");
        if (prop instanceof String value && !value.isBlank()) {
            return value.trim();
        }
        String env = System.getenv("ANDROID_CONFIG_ENV");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return null;
    }
}
