import com.android.build.gradle.LibraryExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jspecify.annotations.NonNull;

import java.util.Properties;

import local.JavaAndroid;
import local.PropertiesLoader;

public class AndroidLibraryConventionPlugin implements Plugin<@NonNull Project> {

    @Override
    public void apply(Project target) {
        target.getPluginManager().apply("com.android.library");

        Properties config = PropertiesLoader.loadPropertiesFile(target, "config.properties");

        int javaVersion = PropertiesLoader.getPropertyInt(config, "javaVersion");
        int compileSdk = PropertiesLoader.getPropertyInt(config, "compileSdk");
        int minSdk = PropertiesLoader.getPropertyInt(config, "minSdk");

        boolean enableViewBinding = PropertiesLoader.getPropertyBoolean(config, "enableViewBinding", true);

        LibraryExtension extension = target.getExtensions().getByType(LibraryExtension.class);
        JavaAndroid.configureJavaAndroid(target, javaVersion, extension, compileSdk, minSdk, enableViewBinding);

        extension.getDefaultConfig().consumerProguardFile("consumer-rules.pro");

        extension.getBuildTypes().configureEach(buildType -> {
            if ("release".equals(buildType.getName())) {
                buildType.setMinifyEnabled(false);
                buildType.proguardFile(extension.getDefaultProguardFile("proguard-android-optimize.txt"));
                buildType.proguardFile(target.getLayout().getProjectDirectory().file("proguard-rules.pro").getAsFile());
            }
        });
    }
}