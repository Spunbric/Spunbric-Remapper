import org.cadixdev.lorenz.io.MappingFormats
import org.cadixdev.lorenz.io.MappingsReader
import org.cadixdev.lorenz.io.MappingsWriter
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Writer
import java.util.Optional

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

task<GenerateSrgToIntermediaryTask>("generateSrgToIntermediary") {
    this.format = MappingFormats.TSRG
    this.outputName = "srgToIntermediary.tsrg"
}

task<GenerateSrgToIntermediaryTask>("generateTinySrgToIntermediary") {
    this.format = WritableTinyFormat
    this.outputName = "srgToIntermediary.tiny"
}

task<GenerateIntermediaryToMcpMappingsTask>("generateIntermediaryToMcp") {
    this.format = WritableTinyFormat
    this.outputName = "intermediaryToMcp.tiny"
}

object WritableTinyFormat : org.cadixdev.lorenz.io.TextMappingFormat {
    override fun createWriter(writer: Writer?): MappingsWriter {
        return TinyV2MappingsWriter(writer, "named", "intermediary")
    }

    override fun getStandardFileExtension(): Optional<String> {
        return Optional.of(".tiny")
    }

    override fun createReader(reader: Reader?): MappingsReader {
        throw UnsupportedOperationException("Use normal tiny reader")
    }
}
