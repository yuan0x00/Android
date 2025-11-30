import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.jetbrains.annotations.NotNull;

import local.JavaLibrary;

public class JavaLibraryConventionPlugin implements Plugin<@NotNull Project> {

    @Override
    public void apply(Project target) {
        target.getPluginManager().apply(JavaLibraryPlugin.class);

//        Properties config = PropertiesLoader.loadPropertiesFile(target, "config.properties");

        JavaLibrary.configureJavaLibrary(target);
    }
}