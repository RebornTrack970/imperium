import com.github.jengelman.gradle.plugins.shadow.internal.RelocationUtil
import com.github.jengelman.gradle.plugins.shadow.relocation.SimpleRelocator
import fr.xpdustry.toxopid.dsl.anukenJitpack
import fr.xpdustry.toxopid.dsl.mindustryDependencies
import fr.xpdustry.toxopid.task.GithubArtifactDownload

plugins {
    id("foundation.base-conventions")
    id("foundation.publishing-conventions")
    id("fr.xpdustry.toxopid")
    id("com.github.johnrengelman.shadow")
}

val metadata = fr.xpdustry.toxopid.spec.ModMetadata.fromJson(project.file("plugin.json"))
metadata.minGameVersion = libs.versions.mindustry.get()
metadata.description = rootProject.description!!
metadata.version = rootProject.version.toString()

toxopid {
    compileVersion.set(libs.versions.mindustry.map { "v$it" })
    platforms.add(fr.xpdustry.toxopid.spec.ModPlatform.HEADLESS)
    useMindustryMirror.set(true)
}

repositories {
    anukenJitpack()
    xpdustryReleases()
}

dependencies {
    api(projects.foundationCommon) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }
    mindustryDependencies()
    compileOnly(libs.distributor.api)
}

tasks.shadowJar {
    archiveFileName.set("FoundationMindustryCore.jar")
    archiveClassifier.set("plugin")

    doFirst {
        val temp = temporaryDir.resolve("plugin.json")
        temp.writeText(metadata.toJson(true))
        from(temp)
    }

    from(rootProject.file("LICENSE.md")) {
        into("META-INF")
    }

    minimize()
    mergeServiceFiles()

    RelocationUtil.configureRelocation(this, "com.xpdustry.foundation.shadow")
    relocators.removeAll { it is SimpleRelocator && it.pattern.startsWith("com.xpdustry.foundation.common") }
}

tasks.register("getArtifactPath") {
    doLast { println(tasks.shadowJar.get().archiveFile.get().toString()) }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

val downloadKotlinRuntime = tasks.register<GithubArtifactDownload>("downloadKotlinRuntime") {
    user.set("Xpdustry")
    repo.set("KotlinRuntimePlugin")
    name.set("KotlinRuntimePlugin.jar")
    version.set(libs.versions.kotlin.map { "v2.0.0-k.$it" })
}

val downloadDistributor = tasks.register<GithubArtifactDownload>("downloadDistributor") {
    user.set("Xpdustry")
    repo.set("Distributor")
    name.set("DistributorCore.jar")
    version.set(libs.versions.distributor.map { "v$it" })
}

tasks.runMindustryClient {
    mods.setFrom()
}

tasks.runMindustryServer {
    mods.setFrom(downloadKotlinRuntime, tasks.shadowJar, downloadDistributor)
}
