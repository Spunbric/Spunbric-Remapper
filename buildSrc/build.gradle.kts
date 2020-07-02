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
}

dependencies {
}
