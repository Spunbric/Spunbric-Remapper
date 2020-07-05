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
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            this.name = "Sonatype Snapshots"
        }
        maven("https://files.minecraftforge.net/maven") {
            this.name = "MinecraftForge"
        }
        maven("https://repo-new.spongepowered.org/repository/maven-public") {
            this.name = "New Sponge"
        }
        maven("https://repo.spongepowered.org/maven") {
            this.name = "Sponge"
        }
    }
}
