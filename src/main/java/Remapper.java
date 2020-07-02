import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

import net.fabricmc.lorenztiny.TinyMappingsReader;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.srg.tsrg.TSrgReader;
import org.cadixdev.lorenz.io.srg.tsrg.TSrgWriter;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.cadixdev.mercury.Mercury;
import org.cadixdev.mercury.mixin.MixinRemapper;
import org.cadixdev.mercury.remapper.MercuryRemapper;

public final class Remapper {
	private static boolean EXPORT_MCP_TO_YARN = false;

	public static void main(String[] args) throws IOException {
		info(":Preparing to remap SpongeCommon");

		final Path projectDir = new File(System.getProperty("user.dir")).toPath();
		final Path destination = projectDir.resolve("remapped");
		final Path mappings = projectDir.resolve("mappings");

		final Path yarnMappings = mappings.resolve("yarn.tiny");
		final Path srgMappings = mappings.resolve("srg.tsrg");

		final Path mcpFieldMappings = mappings.resolve("mcp").resolve("fields.csv");
		final Path mcpMethodMappings = mappings.resolve("mcp").resolve("methods.csv");

		Files.createDirectories(mappings);
		Files.createDirectories(destination);

		// Verify the destination is empty so we don't merge two sets of stuff lol
		if (!MappingUtils.isDirEmpty(destination)) {
			error("Destination path, %s must be empty before remapping!");
			System.exit(2);
		}

		MappingUtils.failIfNotExisting(yarnMappings);
		MappingUtils.failIfNotExisting(srgMappings);
		MappingUtils.failIfNotExisting(mcpFieldMappings);
		MappingUtils.failIfNotExisting(mcpMethodMappings);

		final TinyMappingsReader tinyReader = MappingUtils.readTiny(yarnMappings, "official", "named");

		final TSrgReader tSrgReader = new TSrgReader(new BufferedReader(new FileReader(srgMappings.toFile())));

		final MappingSet officalToYarn = tinyReader.read();
		final MappingSet officalToSrg = tSrgReader.read();

		final MappingSet srgToYarn = officalToSrg.reverse().merge(officalToYarn);
		final MappingSet yarnToSrg = srgToYarn.reverse(); // We can't set obfuscated names lol, gotta reserve it

		final Map<String, String> mcpFields = MappingUtils.readCsv(new Scanner(mcpFieldMappings));
		final Map<String, String> mcpMethods = MappingUtils.readCsv(new Scanner(mcpMethodMappings));

		MappingUtils.iterateClasses(yarnToSrg, classMapping -> {
			for (FieldMapping fieldMapping : classMapping.getFieldMappings()) {
				final String deobfuscatedName = fieldMapping.getDeobfuscatedName();
				fieldMapping.setDeobfuscatedName(mcpFields.getOrDefault(deobfuscatedName, deobfuscatedName));
			}

			for (MethodMapping methodMapping : classMapping.getMethodMappings()) {
				final String deobfuscatedName = methodMapping.getDeobfuscatedName();
				methodMapping.setDeobfuscatedName(mcpMethods.getOrDefault(deobfuscatedName, deobfuscatedName));
			}
		});

		final MappingSet mcpToYarn = yarnToSrg.reverse(); // yarnToSrg is now yarnToMcp

		info(":Generated MCP -> Yarn Mappings");

		if (Remapper.EXPORT_MCP_TO_YARN) {
			info("Exporting MCP -> Yarn Mappings");

			TSrgWriter writer = new TSrgWriter(new FileWriter(projectDir.resolve("artifacts").resolve("mcpToYarn.tsrg").toFile()));
			writer.write(mcpToYarn);
		}

		final Mercury mercury = new Mercury();
		mercury.getProcessors().add(MercuryRemapper.create(mcpToYarn, false));
		mercury.getProcessors().add(MixinRemapper.create(mcpToYarn));

		info(":Remapping SpongeCommon");

		// TODO: Actually remap
	}

	private static <T> void info(T msg) {
		System.out.println(msg);
	}

	private static <T> void error(T msg) {
		System.err.println(msg);
	}

	private Remapper() {
	}
}
