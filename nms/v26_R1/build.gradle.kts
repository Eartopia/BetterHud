import io.papermc.paperweight.userdev.PaperweightUserExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

plugins {
    alias(libs.plugins.conventions.paperweight)
}

val javaToolchains = extensions.getByType<JavaToolchainService>()

extensions.configure<PaperweightUserExtension> {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25))
    })
}

dependencies {
    paperweight.paperDevBundle("26.1.2.build.+")
}
