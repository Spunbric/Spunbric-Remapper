pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
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
}
