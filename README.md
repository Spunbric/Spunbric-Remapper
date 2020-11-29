# Spunbric Remapper

# Defunct: Replaced in favor of https://github.com/Spunbric/spunbric-mappings for MCP in dev

A set of tools used to remap SpongeCommon to yarn mappings for use in Spunbric.

## Cloning the repository

We use submodules to refer to SpongeCommon and MercuryMixin artifacts, so you must clone this repository properly.

If you're cloning the repository for the first time, use the following:

```git clone --recurse-submodules https://github.com/Spunbric/Spunbric-Remapper.git```

To clone submodules in an already cloned repository:

```git submodule update --init --recursive```

Once cloned, make sure you use the following to setup SpongeCommon for remapping:

```gradle applyPatches```

## Setup

Some setup is required since we do not distribute the mappings in this repo.

<details><summary>Setup Process</summary>

### 1. Generate Tiny V2 Merged mappings

Clone yarn (For 1.14.4, use my branch [here](https://github.com/i509VCB/dmcyarn/tree/114hack)).

Run `gradle build` and get the `yarn-MINECRAFT_VERSION+build.local-mergedv2.jar` file generated in `/build/libs/`.

Extract the `mappings.tiny` file inside of the mergedv2 jar, rename to `yarn.tiny` and place the file in the `mappings` folder.

### 2. Get MCPConfig tsrg mappings.

Go to MCPConfig repository and grab the `joined.tsrg` file for the corresponding Minecraft version. Rename it to `srg.tsrg` and place it in the `mappings` folder.

### 3. Get Field and Method mappings

Find the `fields.csv` and `methods.csv` files that map `srg -> mcp`. Place them in the `mappings/mcp` folder.

### 4. Get the 1.14.4 Client jar

You can get the client jar on the [wiki](https://minecraft.gamepedia.com/Java_Edition_1.14.4).
Place it in the `mappings` folder.

</details>

## Licensing

The source code related to remapping here is licensed under the [MIT License](LICENSE.txt).

SpongeCommon is Licensed under the [MIT License](https://github.com/SpongePowered/SpongeCommon/blob/api-8/LICENSE.txt).

MercuryMixin is licensed under [MPL 2.0](https://github.com/CadixDev/MercuryMixin/blob/master/LICENSE.txt)


