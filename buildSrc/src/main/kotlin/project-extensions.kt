import com.github.schaka.build.Build
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getByType

/**
 * Returns a CSV list of absolute paths located in the all subproject report directories.
 *
 * @param include a include pattern.
 * @return a CSV list with absolute paths.
 */
fun Project.collectSubprojectsReportFiles(include: String): String {
    return subprojects
            .joinToString(",") { subproject: Project ->
                subproject.collectReportFiles(include)
            }
}

/**
 * Returns a CSV list of absolute paths of all subproject main source directories.
 *
 * @return a CSV list with absolute paths.
 */
fun Project.collectSubprojectsSourceDirectories(sourceSetName: String): String {
    return subprojects
            .map { it.extensions.getByName("sourceSets") as SourceSetContainer }
            .map { it.getByName(sourceSetName) }
            .flatMap { it.java.srcDirs }
            .joinToString(",")
}

/**
 * Returns a CSV list of absolute paths located in the project report directory.
 *
 * @param include a include pattern.
 * @return a CSV list with absolute paths.
 */
fun Project.collectReportFiles(include: String): String {
    val fileTree = fileTree("${layout.buildDirectory}/reports") {
        include(include)
    }

    return fileTree
            .files
            .joinToString(",")
}

/**
 * Returns a list with the <code>check</code> tasks of the subprojects.
 */
fun Project.subprojectTasks(taskName: String): List<TaskProvider<DefaultTask>> {
    return subprojects
            .map { subproject: Project ->
                subproject.tasks.named(taskName, DefaultTask::class.java)
            }
}

/**
 * @return the SourceSetContainer of the project.
 */
fun Project.getSourceSetContainer(): SourceSetContainer {
    return extensions.getByType(SourceSetContainer::class)
}

/**
 * @return the special information of this build.
 */
fun Project.getBuild(): Build {
    val build: Build
    if (extra.has(Build::class.java.name)) {
        build = extra.get(Build::class.java.name) as Build
    } else {
        build = Build(this)
        extra.set(Build::class.java.name, build)
    }
    return build
}