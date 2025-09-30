package local;

import org.gradle.api.Project;
import org.gradle.api.artifacts.VersionCatalog;
import org.gradle.api.artifacts.VersionCatalogsExtension;

public final class ProjectExtensions {

    private ProjectExtensions() {
    }

    public static VersionCatalog libs(Project project) {
        return project.getExtensions().getByType(VersionCatalogsExtension.class).named("libs");
    }
}
