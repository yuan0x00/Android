import com.android.build.gradle.LibraryExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

import local.JavaAndroid;
import local.PropertiesLoader;

public class AndroidLibraryConventionPlugin implements Plugin<@NotNull Project> {

    @Override
    public void apply(Project target) {
        target.getPluginManager().apply("com.android.library");

        Properties config = PropertiesLoader.loadPropertiesFile(target, PropertiesLoader.CONFIG_PROPERTIES);

        int compileSdk = PropertiesLoader.getPropertyInt(config, "compileSdk");
        int minSdk = PropertiesLoader.getPropertyInt(config, "minSdk");

        boolean enableViewBinding = PropertiesLoader.getPropertyBoolean(config, "enableViewBinding", true);

        LibraryExtension extension = target.getExtensions().getByType(LibraryExtension.class);
        JavaAndroid.configureJavaAndroid(target, extension, compileSdk, minSdk, enableViewBinding);

        extension.getDefaultConfig().consumerProguardFile("consumer-rules.pro");

        extension.getBuildTypes().configureEach(buildType -> {
            if ("release".equals(buildType.getName())) {
                buildType.proguardFile(extension.getDefaultProguardFile("proguard-android-optimize.txt"));
                buildType.proguardFile(target.getLayout().getProjectDirectory().file("proguard-rules.pro").getAsFile());
            }
            if ("debug".equals(buildType.getName())) {
                buildType.proguardFile(extension.getDefaultProguardFile("proguard-android-optimize.txt"));
                buildType.proguardFile(target.getLayout().getProjectDirectory().file("proguard-rules.pro").getAsFile());
            }
        });
    }
}
