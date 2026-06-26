import io.papermc.paperweight.userdev.PaperweightUserExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    id("standard-conventions")
    id("io.papermc.paperweight.userdev")
}

val javaToolchains = extensions.getByType<JavaToolchainService>()

extensions.configure<PaperweightUserExtension> {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    })
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":api:bukkit-api"))
}
