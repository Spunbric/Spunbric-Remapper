import java.nio.file.Path;
import java.util.Objects;

import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.MappingFormat;
import org.cadixdev.lorenz.io.MappingsWriter;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public abstract class AbstractGenerateMappingsTask extends AbstractMappingTask {
	protected AbstractGenerateMappingsTask() {
		this.setGroup("generate mappings");
	}

	@Input public MappingFormat format;
	@Input public String outputName;

	@TaskAction
	public void doTask() throws Exception {
		Objects.requireNonNull(this.format, "Mappings Format must be set to generate mappings");
		Objects.requireNonNull(this.outputName, "Output mappings file name must be set");

		final Project project = this.getProject();
		final Path dir = project.getProjectDir().toPath();
		final Path mappings = dir.resolve("mappings");
		final Path artifacts = dir.resolve("artifacts");

		final MappingSet mappingSet = this.generateMappings(project, mappings, artifacts);

		try (MappingsWriter writer = this.format.createWriter(artifacts.resolve(this.outputName))) {
			writer.write(mappingSet);
		}
	}

	protected abstract MappingSet generateMappings(Project project, Path mappingsFolder, Path artifactsFolder) throws Exception;
}
