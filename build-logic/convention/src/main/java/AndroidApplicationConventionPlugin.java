import com.android.build.api.dsl.ApplicationExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jspecify.annotations.NonNull;

import java.util.Properties;

import local.ConfigResolver;
import local.JavaAndroid;
import local.LocalAndroidApplicationExtension;
import local.PropertiesLoader;

public class AndroidApplicationConventionPlugin implements Plugin<@NonNull Project> {

    @Override
    public void apply(Project target) {
        target.getPluginManager().apply("com.android.application");

        LocalAndroidApplicationExtension localExtension =
                target.getExtensions().create("localAndroid", LocalAndroidApplicationExtension.class, target.getObjects());

        Properties config = PropertiesLoader.loadPropertiesFile(target, ConfigResolver.resolveConfigFile(target));

        int compileSdk = PropertiesLoader.getPropertyInt(config, "compileSdk");
        int minSdk = PropertiesLoader.getPropertyInt(config, "minSdk");
        int targetSdk = PropertiesLoader.getPropertyInt(config, "targetSdk");
        int versionCode = PropertiesLoader.getPropertyInt(config, "versionCode");
        String versionName = PropertiesLoader.getPropertyString(config, "versionName");
        String namespace = PropertiesLoader.getPropertyString(config, "namespace");

        boolean enableViewBinding = PropertiesLoader.getPropertyBoolean(config, "enableViewBinding", true);
        boolean releaseMinifyEnabled = PropertiesLoader.getPropertyBoolean(config, "releaseMinifyEnabled", false);
        boolean releaseShrinkResources = PropertiesLoader.getPropertyBoolean(config, "releaseShrinkResources", false);

        localExtension.getEnableReleaseMinify().convention(releaseMinifyEnabled);
        localExtension.getEnableReleaseResourceShrinking().convention(releaseShrinkResources);

        ApplicationExtension extension = target.getExtensions().getByType(ApplicationExtension.class);
        JavaAndroid.configureJavaAndroid(target, extension, compileSdk, minSdk, enableViewBinding);

        extension.getDefaultConfig().setApplicationId(namespace);
        extension.setNamespace(namespace);
        extension.getDefaultConfig().setTargetSdk(targetSdk);
        extension.getDefaultConfig().setVersionCode(versionCode);
        extension.getDefaultConfig().setVersionName(versionName);

        extension.getBuildTypes().configureEach(buildType -> {
            if ("release".equals(buildType.getName())) {
                buildType.setMinifyEnabled(localExtension.getEnableReleaseMinify().get());
                buildType.setShrinkResources(localExtension.getEnableReleaseResourceShrinking().get());
                buildType.proguardFile(extension.getDefaultProguardFile("proguard-android-optimize.txt"));
                buildType.proguardFile(target.getLayout().getProjectDirectory().file("proguard-rules.pro").getAsFile());
            }
        });
    }
}
