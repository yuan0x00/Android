package local;

import com.android.build.api.dsl.CommonExtension;

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;

import java.io.File;

public final class AndroidJava {

    private static final String PROPERTY_COMPILE_SDK = "compileSdk";
    private static final String PROPERTY_MIN_SDK = "minSdk";
    private static final String PROPERTY_JAVA_VERSION = "javaVersion";
    private static final String PROPERTY_ENABLE_VIEW_BINDING = "enableViewBinding";

    private AndroidJava() {
    }

    public static void configureAndroidProject(Project project, CommonExtension<?, ?, ?, ?, ?, ?> commonExtension) {
        int compileSdk = Integer.parseInt(getRequiredProperty(project, PROPERTY_COMPILE_SDK));
        int minSdk = Integer.parseInt(getRequiredProperty(project, PROPERTY_MIN_SDK));
        String javaVersion = getRequiredProperty(project, PROPERTY_JAVA_VERSION);

        commonExtension.setCompileSdk(compileSdk);
        commonExtension.getDefaultConfig().setMinSdk(minSdk);

        commonExtension.getLint().setAbortOnError(false);
        commonExtension.getLint().setCheckReleaseBuilds(false);

        JavaVersion javaVer = getJavaVersion(javaVersion);
        commonExtension.getCompileOptions().setSourceCompatibility(javaVer);
        commonExtension.getCompileOptions().setTargetCompatibility(javaVer);

        commonExtension.getBuildFeatures().setBuildConfig(true);
        commonExtension.getBuildFeatures().setViewBinding(shouldEnableViewBinding(project));
    }

    private static JavaVersion getJavaVersion(String javaVersionStr) {
        switch (javaVersionStr) {
            case "8":
                return JavaVersion.VERSION_1_8;
            case "11":
                return JavaVersion.VERSION_11;
            case "17":
                return JavaVersion.VERSION_17;
            case "19":
                return JavaVersion.VERSION_19;
            default:
                return JavaVersion.VERSION_11; // 默认值
        }
    }

    private static String getRequiredProperty(Project project, String key) {
        String value = project.getProviders().gradleProperty(key).getOrNull();
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing in gradle.properties: " + key);
        }
        return value;
    }

    private static boolean shouldEnableViewBinding(Project project) {
        String override = project.getProviders().gradleProperty(PROPERTY_ENABLE_VIEW_BINDING).getOrNull();
        if (override != null && !override.isBlank()) {
            return Boolean.parseBoolean(override);
        }
        File layoutDir = project.getLayout().getProjectDirectory().file("src/main/res/layout").getAsFile();
        if (layoutDir.isDirectory()) {
            String[] layoutFiles = layoutDir.list((dir, name) -> name.endsWith(".xml"));
            return layoutFiles != null && layoutFiles.length > 0;
        }
        return false;
    }
}
