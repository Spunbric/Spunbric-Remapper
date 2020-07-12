import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;

import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.gradle.api.Project;

public class GenerateIntermediaryToMcpMappingsTask extends AbstractGenerateMappingsTask {
	@Override
	protected MappingSet generateMappings(Project project, Path mappingsFolder, Path artifactsFolder) throws Exception {
		final Path srgMappings = MappingUtils.validateFile(mappingsFolder.resolve("srg.tsrg"));
		final Path yarnMappings = MappingUtils.validateFile(mappingsFolder.resolve("yarn.tiny"));
		final Path mcpFieldMappings = MappingUtils.validateFile(mappingsFolder.resolve("mcp").resolve("fields.csv"));
		final Path mcpMethodMappings = MappingUtils.validateFile(mappingsFolder.resolve("mcp").resolve("methods.csv"));

		project.getLogger().lifecycle(":Generating SRG -> Intermediary Mappings");
		final MappingSet srgToIntermediary = this.generateSrgToIntermediary(mappingsFolder.resolve("client.jar"), srgMappings, yarnMappings);
		final MappingSet intermediaryToSrg = srgToIntermediary.reverse(); // Reverse since we can't set obf names

		final Map<String, String> mcpFields = MappingUtils.readCsv(new Scanner(mcpFieldMappings));
		final Map<String, String> mcpMethods = MappingUtils.readCsv(new Scanner(mcpMethodMappings));

		project.getLogger().lifecycle(":Generating Intermediary -> MCP Mappings");

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

		return intermediaryToSrg; // This is now Intermediary -> MCP
	}
}
