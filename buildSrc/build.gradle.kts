buildscript {
    repositories {
        maven("https://files.minecraftforge.net/maven")
    }
    dependencies {
        classpath("org.cadixdev", "mercury", "0.1.0-SNAPSHOT")
        classpath(group = "net.minecraftforge.gradle", name = "ForgeGradle", version = "3.+")
    }
}

plugins {
    java
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public") {
        this.name = "Sonatype OSS"
    }
    maven("http://maven.fabricmc.net") {
        this.name = "Fabric"
    }
    maven("https://files.minecraftforge.net/maven")
    maven("https://repo-new.spongepowered.org/repository/maven-public")
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    //implementation(project(":MercuryMixin")) // TODO: Maven local
    implementation("org.cadixdev", "lorenz", "0.5.2")
    implementation("org.cadixdev", "mercury", "0.1.0-SNAPSHOT")
    implementation("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    implementation("net.fabricmc:lorenz-tiny:2.0.0+build.2")
    implementation(group = "net.minecraftforge.gradle", name = "ForgeGradle", version = "3.+")
}

dependencies {
}
