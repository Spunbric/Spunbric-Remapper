plugins {
    java
    id("net.minecrell.licenser") version "0.4.1"
    id("net.minecrell.gitpatcher") version "0.9.0" // Because we modify SpongeCommon build scripts for remapping
}

apply(plugin = "net.minecrell.licenser")

group = "me.i509"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public") {
        this.name = "Sonatype OSS"
    }
    maven("http://maven.fabricmc.net") {
        this.name = "Fabric"
    }
}

dependencies {
    implementation("org.cadixdev", "lorenz", "0.5.2")
    implementation("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    implementation("net.fabricmc:lorenz-tiny:2.0.0+build.2")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

license {
    header = file("LICENSE.txt")

    include("**.java")
    newLine = true
}

patches {
    submodule = "upstream"
    target = file("PatchedSpongeCommon")
    patches = file("patches")
}

task<RemapSpongeCommonTask>("remapCommon") {
    this.group = "remapping"
}
