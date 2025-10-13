import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.jspecify.annotations.NonNull;

import java.util.Properties;

import local.JavaLibrary;
import local.PropertiesLoader;

public class JavaLibraryConventionPlugin implements Plugin<@NonNull Project> {

    @Override
    public void apply(Project target) {
        target.getPluginManager().apply(JavaLibraryPlugin.class);

        Properties config = PropertiesLoader.loadPropertiesFile(target, "config.properties");

        int javaVersion = PropertiesLoader.getPropertyInt(config, "javaVersion");

        JavaLibrary.configureJavaLibrary(target, javaVersion);
    }
}