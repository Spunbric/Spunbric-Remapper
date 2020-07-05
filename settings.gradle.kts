rootProject.name = "CommonTests"

include("SpongeCommon")
include("SpongeCommon:SpongeAPI")
project(":SpongeCommon").projectDir = file("PatchedSpongeCommon")
project(":SpongeCommon:SpongeAPI").projectDir = file("PatchedSpongeCommon/SpongeAPI")
