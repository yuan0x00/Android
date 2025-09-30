package local;

import com.android.build.api.dsl.CommonExtension;

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class AndroidJava {

    private static final String CONFIG_FILE = "gradle.properties";
    private static final String PROPERTY_COMPILE_SDK = "compileSdk";
    private static final String PROPERTY_MIN_SDK = "minSdk";
    private static final String PROPERTY_TARGET_SDK = "targetSdk";

    private AndroidJava() {
    }

    public static void configureAndroidProject(Project project, CommonExtension<?, ?, ?, ?, ?, ?> commonExtension) {
        Properties properties = loadProperties(project);

        int compileSdk = Integer.parseInt(getRequiredProperty(properties, PROPERTY_COMPILE_SDK));
        int minSdk = Integer.parseInt(getRequiredProperty(properties, PROPERTY_MIN_SDK));
        int targetSdk = Integer.parseInt(getRequiredProperty(properties, PROPERTY_TARGET_SDK));

        commonExtension.setCompileSdk(compileSdk);
        commonExtension.getDefaultConfig().setMinSdk(minSdk);
//        commonExtension.getDefaultConfig().setTargetSdk(targetSdk);
        commonExtension.getCompileOptions().setSourceCompatibility(JavaVersion.VERSION_11);
        commonExtension.getCompileOptions().setTargetCompatibility(JavaVersion.VERSION_11);
        commonExtension.getBuildFeatures().setBuildConfig(true);
        commonExtension.getBuildFeatures().setViewBinding(true);
    }

    private static Properties loadProperties(Project project) {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(project.getRootProject().file(CONFIG_FILE))) {
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read gradle.properties", exception);
        }
        return properties;
    }

    private static String getRequiredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing in gradle.properties: " + key);
        }
        return value;
    }
}
