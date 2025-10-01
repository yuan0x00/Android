import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.jspecify.annotations.NonNull;

import local.JavaConfiguration;

public class JavaLibraryConventionPlugin implements Plugin<@NonNull Project> {

    @Override
    public void apply(Project target) {
        target.getPluginManager().apply(JavaLibraryPlugin.class);

        JavaConfiguration.configureJavaProject(target);
    }
}