import java.nio.file.Path;

import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.MappingFormats;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

public class GenerateMappingTask extends AbstractMappingTask {
	@TaskAction
	public void doTask() throws Exception {
		final Project project = this.getProject();
		final Path dir = project.getProjectDir().toPath();

		final MappingSet mappingSet = this.generateMcpToYarnMappings();

		MappingFormats.TSRG.createWriter(dir.resolve("artifacts").resolve("test.tsrg")).write(mappingSet);

		// Make a srg -> mcp
		final Path mappings = dir.resolve("mappings");
		final Path srgMappings = mappings.resolve("srg.tsrg");
		final Path mcpFieldMappings = mappings.resolve("mcp").resolve("fields.csv");
		final Path mcpMethodMappings = mappings.resolve("mcp").resolve("methods.csv");
		final MappingSet srgToMcp = this.generateMcpToSrg(srgMappings, mcpFieldMappings, mcpMethodMappings);

		MappingFormats.TSRG.createWriter(dir.resolve("artifacts").resolve("srgToMcp.tsrg")).write(srgToMcp);
	}
}
