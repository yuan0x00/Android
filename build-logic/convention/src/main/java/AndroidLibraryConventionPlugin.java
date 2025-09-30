import com.android.build.gradle.LibraryExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jspecify.annotations.NonNull;

import local.AndroidJava;

public class AndroidLibraryConventionPlugin implements Plugin<@NonNull Project> {

    @Override
    public void apply(Project target) {
        target.getPluginManager().apply("com.android.library");

        LibraryExtension extension = target.getExtensions().getByType(LibraryExtension.class);
        extension.getDefaultConfig().consumerProguardFile("consumer-rules.pro");

        AndroidJava.configureAndroidProject(target, extension);
    }
}
