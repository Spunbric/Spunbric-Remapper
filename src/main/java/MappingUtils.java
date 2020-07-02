import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

import net.fabricmc.lorenztiny.TinyMappingsReader;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.InnerClassMapping;
import org.cadixdev.lorenz.model.TopLevelClassMapping;

final class MappingUtils {
	static Map<String, String> readCsv(Scanner csv) {
		Map<String, String> mappings = new LinkedHashMap<>();

		try (Scanner scanner = csv) {
			scanner.nextLine();

			while (scanner.hasNextLine()) {
				String[] parts = scanner.nextLine().split(",");
				mappings.put(parts[0], parts[1]);
			}
		}

		return mappings;
	}

	static boolean isDirEmpty(Path path) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			return !stream.iterator().hasNext();
		}
	}

	static void iterateClasses(MappingSet inputMappings, Consumer<ClassMapping<?, ?>> consumer) {
		for (TopLevelClassMapping classMapping : inputMappings.getTopLevelClassMappings()) {
			iterateClass(classMapping, consumer);
		}
	}

	static void iterateClass(ClassMapping<?, ?> classMapping, Consumer<ClassMapping<?, ?>> consumer) {
		consumer.accept(classMapping);

		for (InnerClassMapping innerClassMapping : classMapping.getInnerClassMappings()) {
			iterateClass(innerClassMapping, consumer);
		}
	}

	static void failIfNotExisting(Path path) throws FileNotFoundException {
		if (Files.notExists(path)) {
			throw new FileNotFoundException(String.format("File at %s does not exist", path));
		}
	}

	static TinyMappingsReader readTiny(Path path, String from, String to) throws IOException {
		final TinyTree tinyTree = TinyMappingFactory.loadWithDetection(new BufferedReader(new FileReader(path.toFile())));

		if (!tinyTree.getMetadata().getNamespaces().contains(from)) {
			throw new RuntimeException(String.format("Could not find mapping namespace %s", from));
		}

		if (!tinyTree.getMetadata().getNamespaces().contains(to)) {
			throw new RuntimeException(String.format("Could not find mapping namespace %s", to));
		}

		return new TinyMappingsReader(tinyTree, from, to);
	}
}
