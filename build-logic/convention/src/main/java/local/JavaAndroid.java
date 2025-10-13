package local;

import com.android.build.api.dsl.CommonExtension;

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;

public final class JavaAndroid {

    private JavaAndroid() {
    }

    public static void configureJavaAndroid(
            Project project,
            CommonExtension<?, ?, ?, ?, ?, ?> commonExtension,
            int compileSdk,
            int minSdk,
            boolean enableViewBinding
    ) {
        commonExtension.setCompileSdk(compileSdk);
        commonExtension.getDefaultConfig().setMinSdk(minSdk);

        commonExtension.getLint().setAbortOnError(false);
        commonExtension.getLint().setCheckReleaseBuilds(false);

        commonExtension.getCompileOptions().setSourceCompatibility(JavaVersion.VERSION_17);
        commonExtension.getCompileOptions().setTargetCompatibility(JavaVersion.VERSION_17);

        commonExtension.getBuildFeatures().setBuildConfig(true);
        commonExtension.getBuildFeatures().setViewBinding(enableViewBinding);
    }
}