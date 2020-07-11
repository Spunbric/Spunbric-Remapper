import java.nio.file.Path;

import org.cadixdev.lorenz.MappingSet;
import org.gradle.api.Project;

public class GenerateSrgToIntermediaryTask extends AbstractGenerateMappingsTask {
	@Override
	protected MappingSet generateMappings(Project project, Path mappingsFolder, Path artifactsFolder) throws Exception {
		final Path srgMappings = MappingUtils.validateFile(mappingsFolder.resolve("srg.tsrg"));
		final Path yarnMappings = MappingUtils.validateFile(mappingsFolder.resolve("yarn.tiny"));

		project.getLogger().lifecycle(":Generating SRG -> Intermediary Mappings");

		return this.generateSrgToIntermediary(mappingsFolder.resolve("client.jar"), srgMappings, yarnMappings);
	}
}
