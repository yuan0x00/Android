package local;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

public final class JavaConfiguration {

    private static final String PROPERTY_JAVA_VERSION = "javaVersion";

    private JavaConfiguration() {
    }

    public static void configureJavaProject(Project project) {
        String javaVersionStr = getRequiredProperty(project);
        int javaVersion = Integer.parseInt(javaVersionStr);

        JavaPluginExtension extension = project.getExtensions().getByType(JavaPluginExtension.class);
        extension.toolchain(javaToolchainSpec -> javaToolchainSpec.getLanguageVersion().set(JavaLanguageVersion.of(javaVersion)));

        // 配置所有 JavaCompile 任务
        project.getTasks().withType(JavaCompile.class).configureEach(javaCompile -> javaCompile.getOptions().getRelease().set(javaVersion));
    }

    private static String getRequiredProperty(Project project) {
        String value = project.getProviders().gradleProperty(JavaConfiguration.PROPERTY_JAVA_VERSION).getOrNull();
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing in gradle.properties: " + JavaConfiguration.PROPERTY_JAVA_VERSION);
        }
        return value;
    }
}
