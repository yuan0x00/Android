import com.android.build.api.dsl.ApplicationExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jspecify.annotations.NonNull;

import java.util.Properties;

import local.JavaAndroid;
import local.PropertiesLoader;

public class AndroidApplicationConventionPlugin implements Plugin<@NonNull Project> {

    @Override
    public void apply(Project target) {
        target.getPluginManager().apply("com.android.application");

        Properties config = PropertiesLoader.loadPropertiesFile(target, "config.properties");

        int javaVersion = PropertiesLoader.getPropertyInt(config, "javaVersion");
        int compileSdk = PropertiesLoader.getPropertyInt(config, "compileSdk");
        int minSdk = PropertiesLoader.getPropertyInt(config, "minSdk");
        int targetSdk = PropertiesLoader.getPropertyInt(config, "targetSdk");

        boolean enableViewBinding = PropertiesLoader.getPropertyBoolean(config, "enableViewBinding", true);

        ApplicationExtension extension = target.getExtensions().getByType(ApplicationExtension.class);
        JavaAndroid.configureJavaAndroid(target, javaVersion, extension, compileSdk, minSdk, enableViewBinding);

        extension.getDefaultConfig().setTargetSdk(targetSdk);

        extension.getBuildTypes().configureEach(buildType -> {
            if ("release".equals(buildType.getName())) {
                buildType.setMinifyEnabled(false);
                buildType.proguardFile(extension.getDefaultProguardFile("proguard-android-optimize.txt"));
                buildType.proguardFile(target.getLayout().getProjectDirectory().file("proguard-rules.pro").getAsFile());
            }
        });
    }
}