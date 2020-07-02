plugins {
    java
}

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
    implementation(project(":MercuryMixin"))
    implementation("org.cadixdev", "lorenz", "0.5.2")
    implementation("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    implementation("net.fabricmc:lorenz-tiny:2.0.0+build.2")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
