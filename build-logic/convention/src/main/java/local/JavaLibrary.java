package local;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

public final class JavaLibrary {

    private JavaLibrary() {
    }

    public static void configureJavaLibrary(Project project) {

        JavaPluginExtension extension = project.getExtensions().getByType(JavaPluginExtension.class);
        extension.toolchain(javaToolchainSpec -> javaToolchainSpec.getLanguageVersion().set(JavaLanguageVersion.of(17)));

        project.getTasks().withType(JavaCompile.class).configureEach(javaCompile -> javaCompile.getOptions().getRelease().set(17));
    }
}
