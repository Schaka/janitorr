import com.github.schaka.build.Build
import org.gradle.api.Project
import org.gradle.internal.extensions.core.extra

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