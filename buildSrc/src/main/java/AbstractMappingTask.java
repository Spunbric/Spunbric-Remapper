import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.cadixdev.bombe.analysis.CachingInheritanceProvider;
import org.cadixdev.bombe.analysis.CascadingInheritanceProvider;
import org.cadixdev.bombe.analysis.InheritanceProvider;
import org.cadixdev.bombe.analysis.ReflectionInheritanceProvider;
import org.cadixdev.bombe.asm.analysis.ClassProviderInheritanceProvider;
import org.cadixdev.bombe.asm.jar.ClassProvider;
import org.cadixdev.bombe.asm.jar.JarFileClassProvider;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.asm.LorenzRemapper;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.MethodMapping;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

public abstract class AbstractMappingTask extends DefaultTask {
	protected MappingSet generateMcpToYarnMappings() throws Exception {
		final Project project = this.getProject();
		final Path dir = project.getProjectDir().toPath();

		final Path mappings = dir.resolve("mappings");
		final Path yarnMappings = mappings.resolve("yarn.tiny");
		final Path srgMappings = mappings.resolve("srg.tsrg");
		final Path clientJar = mappings.resolve("client.jar");

		final Path mcpFieldMappings = mappings.resolve("mcp").resolve("fields.csv");
		final Path mcpMethodMappings = mappings.resolve("mcp").resolve("methods.csv");

		MappingUtils.validateFile(clientJar);
		MappingUtils.validateFile(yarnMappings);
		MappingUtils.validateFile(srgMappings);
		MappingUtils.validateFile(mcpFieldMappings);
		MappingUtils.validateFile(mcpMethodMappings);

		final MappingSet srgToIntermediary = this.generateSrgToIntermediary(clientJar, srgMappings, yarnMappings);
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

		final MappingSet intermediaryToNamed = MappingUtils.readTiny(yarnMappings, "intermediary", "named"); // intermediary -> named

		// noinspection UnnecessaryLocalVariable - Now mcp -> intermediary
		final MappingSet mcpToIntermediary = intermediaryToSrg;
		final MappingSet mcpToYarn = mcpToIntermediary.reverse().merge(intermediaryToNamed);

		this.getLogger().lifecycle(":Generated MCP -> Yarn Mappings");

		return mcpToYarn;
	}

	protected MappingSet generateSrgToIntermediary(Path clientJar, Path srgMappings, Path yarnMappings) throws Exception {
		final MappingSet officialToSrg = MappingUtils.readTsrg(srgMappings); // obf -> srg
		final MappingSet officialToIntermediary = MappingUtils.readTiny(yarnMappings, "official", "intermediary"); // obf -> intermediary

		// Generate propagation for intermediary
		// TODO: Make propagation actually sane
		try (JarFile clientJarFile = new JarFile(clientJar.toFile())) {
			final CascadingInheritanceProvider cascadingInheritanceProvider = new CascadingInheritanceProvider();
			final List<ClassProvider> providers = new ArrayList<>();

			// Add the obfuscated client jar for context
			providers.add(new JarFileClassProvider(clientJarFile));

			cascadingInheritanceProvider.install(new ClassProviderInheritanceProvider(klass -> {
				System.out.println(klass);
				for (ClassProvider provider : providers) {
					final byte[] bytes = provider.get(klass);

					if (bytes != null) {
						System.out.println("found " + klass);
						return bytes;
					}
				}

				return null;
			}));

			// Install JRE classes
			cascadingInheritanceProvider.install(new ReflectionInheritanceProvider(ClassLoader.getSystemClassLoader()));

			// Install all dependencies - this is needed to make sure everything goes well
			final Project spongeCommon = this.getProject().findProject(":SpongeCommon");
			final Set<Path> files = spongeCommon.getConfigurations()
					.getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME)
					.getFiles()
					.stream()
					.map(File::toPath)
					.collect(Collectors.toSet());

			for (Path file : files) {
				if (Files.isRegularFile(file)) {
					providers.add(new JarFileClassProvider(new JarFile(file.toFile())));
				}
			}

			final InheritanceProvider provider = new CachingInheritanceProvider(cascadingInheritanceProvider);

			// Complete the mappings
			MappingUtils.iterateClasses(officialToIntermediary, classMapping -> classMapping.complete(provider));

			// SRGs do not have field signatures, get them from tiny mappings
			officialToSrg.addFieldTypeProvider(MappingUtils.typeProviderFromMappings(officialToIntermediary));

			// This should be all propagated now
			return this.generateSrgToIntermediary(officialToSrg, officialToIntermediary);
		}
	}

	protected MappingSet generateSrgToIntermediary(MappingSet officialToSrg, MappingSet officialToIntermediary) {
		return officialToSrg
				.reverse()
				.merge(officialToIntermediary);
	}

	protected MappingSet generateSrgToMcp(Path srg, Path fields, Path methods) throws Exception {
		final MappingSet officialToSrg = MappingUtils.readTsrg(srg);

		// Scanners are closed by readCsv
		final Map<String, String> mcpFields = MappingUtils.readCsv(new Scanner(fields));
		final Map<String, String> mcpMethods = MappingUtils.readCsv(new Scanner(methods));

		return this.generateSrgToMcp(officialToSrg, mcpFields, mcpMethods);
	}

	protected MappingSet generateSrgToMcp(MappingSet officialToSrg, Map<String, String> fields, Map<String, String> methods) {
		final MappingSet srgToSrg = officialToSrg
				.copy()
				.reverse() // srg -> official
				.merge(officialToSrg); // srg -> (official -> official) -> srg

		MappingUtils.iterateClasses(srgToSrg, classMapping -> {
			for (FieldMapping fieldMapping : classMapping.getFieldMappings()) {
				final String deobfuscatedName = fieldMapping.getDeobfuscatedName();
				fieldMapping.setDeobfuscatedName(fields.getOrDefault(deobfuscatedName, deobfuscatedName));
			}

			for (MethodMapping methodMapping : classMapping.getMethodMappings()) {
				final String deobfuscatedName = methodMapping.getDeobfuscatedName();
				methodMapping.setDeobfuscatedName(methods.getOrDefault(deobfuscatedName, deobfuscatedName));
			}
		});

		return srgToSrg; // Now SRG -> MCP
	}
}
