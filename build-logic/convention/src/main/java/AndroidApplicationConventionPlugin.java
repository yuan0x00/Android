import com.android.build.api.dsl.ApplicationExtension;
import com.android.build.api.dsl.SigningConfig;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.VersionCatalog;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Properties;

import local.JavaAndroid;
import local.PropertiesLoader;

public class AndroidApplicationConventionPlugin implements Plugin<@NotNull Project> {

    @Override
    public void apply(Project target) {
        target.getPluginManager().apply("com.android.application");

        Properties config = PropertiesLoader.loadPropertiesFile(target, PropertiesLoader.CONFIG_PROPERTIES);

        int compileSdk = PropertiesLoader.getPropertyInt(config, "compileSdk");
        int minSdk = PropertiesLoader.getPropertyInt(config, "minSdk");
        int targetSdk = PropertiesLoader.getPropertyInt(config, "targetSdk");
        int versionCode = PropertiesLoader.getPropertyInt(config, "versionCode");
        String versionName = PropertiesLoader.getPropertyString(config, "versionName");
        String namespace = PropertiesLoader.getPropertyString(config, "namespace");

        boolean enableViewBinding = PropertiesLoader.getPropertyBoolean(config, "enableViewBinding", true);

        ApplicationExtension extension = target.getExtensions().getByType(ApplicationExtension.class);
        JavaAndroid.configureJavaAndroid(target, extension, compileSdk, minSdk, enableViewBinding);

        extension.getDefaultConfig().setApplicationId(namespace);
        extension.setNamespace(namespace);
        extension.getDefaultConfig().setTargetSdk(targetSdk);
        extension.getDefaultConfig().setVersionCode(versionCode);
        extension.getDefaultConfig().setVersionName(versionName);

        // 配置签名
        configureSigning(target, extension);

        extension.getBuildTypes().configureEach(buildType -> {
            if ("release".equals(buildType.getName())) {
                boolean releaseMinifyEnabled = PropertiesLoader.getPropertyBoolean(config, "releaseMinifyEnabled", false);
                boolean releaseShrinkResources = PropertiesLoader.getPropertyBoolean(config, "releaseShrinkResources", false);

                buildType.setMinifyEnabled(releaseMinifyEnabled);
                buildType.setShrinkResources(releaseShrinkResources);
                buildType.proguardFile(extension.getDefaultProguardFile("proguard-android-optimize.txt"));
                buildType.proguardFile(target.getLayout().getProjectDirectory().file("proguard-rules.pro").getAsFile());

                // 为release构建类型设置签名
                buildType.setSigningConfig(extension.getSigningConfigs().findByName("release"));
            }
            if ("debug".equals(buildType.getName())) {
                // 注意：这里通常debug不开启混淆，除非特殊测试需求
                boolean debugMinifyEnabled = PropertiesLoader.getPropertyBoolean(config, "debugMinifyEnabled", false);
                boolean debugShrinkResources = PropertiesLoader.getPropertyBoolean(config, "debugShrinkResources", false);

                if (!debugMinifyEnabled) {
                    VersionCatalogsExtension catalogs = target.getExtensions().getByType(VersionCatalogsExtension.class);
                    VersionCatalog libs = catalogs.named("libs");
                    libs.findLibrary("leakcanary.android").ifPresent(lib ->
                            target.getDependencies().add("debugImplementation", lib.get().toString())
                    );
                }

                buildType.setDebuggable(!debugMinifyEnabled);
                buildType.setMinifyEnabled(debugMinifyEnabled);
                buildType.setShrinkResources(debugShrinkResources);
                buildType.proguardFile(extension.getDefaultProguardFile("proguard-android-optimize.txt"));
                buildType.proguardFile(target.getLayout().getProjectDirectory().file("proguard-rules.pro").getAsFile());

                // 为debug构建类型设置签名
                buildType.setSigningConfig(extension.getSigningConfigs().findByName("debug"));
            }
        });
    }

    private void configureSigning(Project project, ApplicationExtension extension) {
        Properties keystoreProperties = PropertiesLoader.loadPropertiesFile(project, "keystore.properties");

        if (!keystoreProperties.isEmpty()) {
            // 配置release签名
            extension.getSigningConfigs().create("release", signingConfig -> configureSigningConfig(project, signingConfig, keystoreProperties));

            // 配置debug签名 - 使用相同的配置
            SigningConfig debugConfig = extension.getSigningConfigs().findByName("debug");
            if (debugConfig != null) {
                configureSigningConfig(project, debugConfig, keystoreProperties);
            }

            project.getLogger().lifecycle("Signing configured from keystore.properties");
        } else {
            project.getLogger().warn("keystore.properties not found, using default signing");
        }
    }

    private void configureDebugSigning(Project project, SigningConfig signingConfig) {
        signingConfig.setStoreFile(project.getRootProject().file("debug.keystore"));
        signingConfig.setStorePassword("android");
        signingConfig.setKeyAlias("androiddebugkey");
        signingConfig.setKeyPassword("android");
    }

    private void configureSigningConfig(Project project, SigningConfig signingConfig, Properties keystoreProperties) {
        String storeFile = keystoreProperties.getProperty("storeFile");
        String storePassword = keystoreProperties.getProperty("storePassword");
        String keyAlias = keystoreProperties.getProperty("keyAlias");
        String keyPassword = keystoreProperties.getProperty("keyPassword");

        if (storeFile != null) {
            File store = project.getRootProject().file(storeFile);
            if (store.exists()) {
                signingConfig.setStoreFile(store);
            } else {
                project.getLogger().warn("Keystore file not found: {}", store.getAbsolutePath());
                // 如果文件不存在，回退到debug签名
                configureDebugSigning(project, signingConfig);
                return;
            }
        } else {
            project.getLogger().warn("storeFile not specified in properties");
            configureDebugSigning(project, signingConfig);
            return;
        }

        if (storePassword != null) {
            signingConfig.setStorePassword(storePassword);
        } else {
            project.getLogger().warn("storePassword not specified in properties");
        }

        if (keyAlias != null) {
            signingConfig.setKeyAlias(keyAlias);
        } else {
            project.getLogger().warn("keyAlias not specified in properties");
        }

        if (keyPassword != null) {
            signingConfig.setKeyPassword(keyPassword);
        } else {
            project.getLogger().warn("keyPassword not specified in properties");
        }
    }
}