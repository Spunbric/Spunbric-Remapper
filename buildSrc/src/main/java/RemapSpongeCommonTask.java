import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.mercury.Mercury;
import org.cadixdev.mercury.remapper.MercuryRemapper;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskAction;
import util.MappingUtils;

public class RemapSpongeCommonTask extends AbstractMappingTask {

	@TaskAction
	public void doTask() throws Exception {
		final Project project = this.getProject();
		final Path dir = project.getProjectDir().toPath();
		final Path destination = dir.resolve("remapped").resolve("SpongeCommon");
		final Path spongeCommonPath = dir.resolve("PatchedSpongeCommon");

		if (Files.notExists(destination)) {
			Files.createDirectories(destination);
		}

		if (!MappingUtils.isDirEmpty(destination)) {
			throw new RuntimeException("Remapped destination directory must be empty!");
		}

		final MappingSet mcpToYarn = this.generateMcpToYarnMappings();
		final Project spongeCommon = project.findProject(":SpongeCommon");

		//noinspection ConstantConditions
		final Set<Path> minecraftAndDeps = spongeCommon.getConfigurations()
				.getByName("minecraft")
				.getFiles()
				.stream()
				.map(File::toPath)
				.collect(Collectors.toSet());

		// TODO: Add API, Mixin, and all other common deps to classpath
		final Set<Path> files = spongeCommon.getConfigurations()
				.getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME)
				.getFiles()
				.stream()
				.map(File::toPath)
				.collect(Collectors.toSet());

		// Exclude the build artifacts
		files.removeIf(path -> path.startsWith(spongeCommonPath.resolve("build").resolve("classes")));
		files.removeIf(path -> path.startsWith(spongeCommonPath.resolve("build").resolve("resources")));

		for (Path file : files) {
			System.out.println(file);
		}

		// Remove Sponge artifacts
		files.removeIf(path -> path.startsWith(spongeCommonPath));

		this.getLogger().lifecycle(":Remapping SpongeCommon");

		final Mercury mercury = new Mercury();

		// Fails currently on:
		// - BlockStateDirectionDataProvider
		mercury.getProcessors().add(MercuryRemapper.create(mcpToYarn, false));

		// Add all minecraft dependencies to the classpath
		mercury.getClassPath().addAll(files);
		mercury.getClassPath().addAll(minecraftAndDeps);

		// We *may* need this, try to remove reliance on it
		mercury.setGracefulClasspathChecks(true);

		// We need to rewrite in the following order:
		// (Accessors|Launch) -> Main -> Mixins -> Tests
		// We do not rewrite invalid
		this.getLogger().lifecycle(":Rewriting accessors");
		mercury.rewrite(spongeCommonPath.resolve("src/accessors/java"), destination.resolve("src/accessors/java"));

		this.getLogger().lifecycle(":Rewriting launch");
		mercury.rewrite(spongeCommonPath.resolve("src/launch/java"), destination.resolve("src/launch/java"));

		// We need to add launch and accessors to classpath now in order to remap properly
		mercury.getClassPath().add(spongeCommonPath.resolve("src/accessors/java"));
		mercury.getClassPath().add(spongeCommonPath.resolve("src/launch/java"));

		this.getLogger().lifecycle(":Rewriting main");
		mercury.rewrite(spongeCommonPath.resolve("src/main/java"), destination.resolve("src/main/java"));

		// Add main to the classpath so we rewrite mixins correctly
		mercury.getClassPath().add(spongeCommonPath.resolve("src/main/java"));

		this.getLogger().lifecycle(":Rewriting mixins");
		mercury.rewrite(spongeCommonPath.resolve("src/mixins/java"), destination.resolve("src/mixins/java"));

		// All tests are invalid, do not rewrite yet
		//mercury.rewrite(dir.resolve("SpongeCommon").resolve("src/test/java"), destination.resolve("SpongeCommon").resolve("src/test/java"));
	}
}
