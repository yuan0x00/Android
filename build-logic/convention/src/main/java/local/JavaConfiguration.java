package local;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class JavaConfiguration {

    private static final String CONFIG_FILE = "gradle.properties";
    private static final String PROPERTY_JAVA_VERSION = "javaVersion";

    private JavaConfiguration() {
    }

    public static void configureJavaProject(Project project) {
        Properties properties = loadProperties(project);
        String javaVersionStr = getRequiredProperty(properties);
        int javaVersion = Integer.parseInt(javaVersionStr);

        JavaPluginExtension extension = project.getExtensions().getByType(JavaPluginExtension.class);
        extension.toolchain(javaToolchainSpec -> javaToolchainSpec.getLanguageVersion().set(JavaLanguageVersion.of(javaVersion)));

        // 配置所有 JavaCompile 任务
        project.getTasks().withType(JavaCompile.class).configureEach(javaCompile -> javaCompile.getOptions().getRelease().set(javaVersion));
    }

    private static Properties loadProperties(Project project) {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(project.getRootProject().file(CONFIG_FILE))) {
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read gradle.properties", exception);
        }
        return properties;
    }

    private static String getRequiredProperty(Properties properties) {
        String value = properties.getProperty(JavaConfiguration.PROPERTY_JAVA_VERSION);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing in gradle.properties: " + JavaConfiguration.PROPERTY_JAVA_VERSION);
        }
        return value;
    }
}