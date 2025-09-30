import com.android.build.api.dsl.ApplicationExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jspecify.annotations.NonNull;

import local.AndroidJava;

public class AndroidApplicationConventionPlugin implements Plugin<@NonNull Project> {

    @Override
    public void apply(Project target) {
        target.getPluginManager().apply("com.android.application");

        ApplicationExtension extension = target.getExtensions().getByType(ApplicationExtension.class);
        AndroidJava.configureAndroidProject(target, extension);
    }
}
