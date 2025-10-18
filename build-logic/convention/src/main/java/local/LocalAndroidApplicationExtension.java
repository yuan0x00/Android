package local;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/**
 * 自定义扩展，用于控制本地 Android Application 插件的行为。
 */
public class LocalAndroidApplicationExtension {

    private final Property<Boolean> enableReleaseMinify;
    private final Property<Boolean> enableReleaseResourceShrinking;

    @Inject
    public LocalAndroidApplicationExtension(ObjectFactory objects) {
        this.enableReleaseMinify = objects.property(Boolean.class).convention(false);
        this.enableReleaseResourceShrinking = objects.property(Boolean.class).convention(false);
    }

    public Property<Boolean> getEnableReleaseMinify() {
        return enableReleaseMinify;
    }

    public Property<Boolean> getEnableReleaseResourceShrinking() {
        return enableReleaseResourceShrinking;
    }
}
