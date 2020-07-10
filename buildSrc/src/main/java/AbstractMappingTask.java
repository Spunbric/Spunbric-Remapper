import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import util.MappingUtils;

public abstract class AbstractMappingTask extends DefaultTask {
	protected MappingSet generateMcpToYarnMappings() throws Exception {
		final Project project = this.getProject();
		final Path dir = project.getProjectDir().toPath();

		final Path mappings = dir.resolve("mappings");
		final Path yarnMappings = mappings.resolve("yarn.tiny");
		final Path srgMappings = mappings.resolve("srg.tsrg");

		final Path mcpFieldMappings = mappings.resolve("mcp").resolve("fields.csv");
		final Path mcpMethodMappings = mappings.resolve("mcp").resolve("methods.csv");

		MappingUtils.failIfNotExisting(yarnMappings);
		MappingUtils.failIfNotExisting(srgMappings);
		MappingUtils.failIfNotExisting(mcpFieldMappings);
		MappingUtils.failIfNotExisting(mcpMethodMappings);

		final MappingSet intermediaryToNamed = MappingUtils.readTiny(yarnMappings, "intermediary", "named"); // intermediary -> named
		final MappingSet officialToIntermediary = MappingUtils.readTiny(yarnMappings, "official", "intermediary"); // obf -> intermediary

		final MappingSet officialToSrg = MappingUtils.readTsrg(srgMappings); // obf -> srg

		officialToSrg.addFieldTypeProvider(MappingUtils.typeProviderFromMappings(officialToIntermediary));

		final MappingSet srgToIntermediary = officialToSrg.reverse().merge(officialToIntermediary); // srg -> intermediary

		final MappingSet intermediaryToSrg = srgToIntermediary.reverse(); // We can't set obfuscated names lol, gotta reserve it

		final Map<String, String> mcpFields = MappingUtils.readCsv(new Scanner(mcpFieldMappings));
		final Map<String, String> mcpMethods = MappingUtils.readCsv(new Scanner(mcpMethodMappings));

		MappingUtils.iterateClasses(intermediaryToSrg, classMapping -> {
			for (FieldMapping fieldMapping : classMapping.getFieldMappings()) {
				final String deobfuscatedName = fieldMapping.getDeobfuscatedName();
				fieldMapping.setDeobfuscatedName(mcpFields.getOrDefault(deobfuscatedName, deobfuscatedName));
			}

			for (MethodMapping methodMapping : classMapping.getMethodMappings()) {
				final String deobfuscatedName = methodMapping.getDeobfuscatedName();
				methodMapping.setDeobfuscatedName(mcpMethods.getOrDefault(deobfuscatedName, deobfuscatedName));
			}
		});

		// noinspection UnnecessaryLocalVariable - Now mcp -> intermediary
		final MappingSet mcpToIntermediary = intermediaryToSrg;
		final MappingSet mcpToYarn = mcpToIntermediary.reverse().merge(intermediaryToNamed);

		this.getLogger().lifecycle(":Generated MCP -> Yarn Mappings");

		return mcpToYarn;
	}

	protected MappingSet generateSrgToMcp(Path srg, Path fields, Path methods) throws Exception {
		final MappingSet officialToSrg = MappingUtils.readTsrg(srg);

		final MappingSet srgToSrg = officialToSrg
				.copy()
				.reverse() // srg -> official
				.merge(officialToSrg); // srg -> (official -> official) -> srg

		// Scanners are closed by readCsv
		final Map<String, String> mcpFields = MappingUtils.readCsv(new Scanner(fields));
		final Map<String, String> mcpMethods = MappingUtils.readCsv(new Scanner(methods));

		MappingUtils.iterateClasses(srgToSrg, classMapping -> {
			for (FieldMapping fieldMapping : classMapping.getFieldMappings()) {
				final String deobfuscatedName = fieldMapping.getDeobfuscatedName();
				fieldMapping.setDeobfuscatedName(mcpFields.getOrDefault(deobfuscatedName, deobfuscatedName));
			}

			for (MethodMapping methodMapping : classMapping.getMethodMappings()) {
				final String deobfuscatedName = methodMapping.getDeobfuscatedName();
				methodMapping.setDeobfuscatedName(mcpMethods.getOrDefault(deobfuscatedName, deobfuscatedName));
			}
		});

		return srgToSrg; // This is now SRG -> MCP
	}
}
