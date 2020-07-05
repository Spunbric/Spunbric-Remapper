/*
 * MIT License
 *
 * Copyright (c) i509VCB<git@i509.me>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package util;

import java.io.BufferedReader;
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
import org.cadixdev.lorenz.io.srg.tsrg.TSrgReader;
import org.cadixdev.lorenz.model.ClassMapping;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.InnerClassMapping;
import org.cadixdev.lorenz.model.TopLevelClassMapping;
import org.cadixdev.lorenz.model.jar.FieldTypeProvider;

public final class MappingUtils {
	public static Map<String, String> readCsv(Scanner csv) {
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

	public static boolean isDirEmpty(Path path) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			return !stream.iterator().hasNext();
		}
	}

	public static void iterateClasses(MappingSet inputMappings, Consumer<ClassMapping<?, ?>> consumer) {
		for (TopLevelClassMapping classMapping : inputMappings.getTopLevelClassMappings()) {
			iterateClass(classMapping, consumer);
		}
	}

	public static void iterateClass(ClassMapping<?, ?> classMapping, Consumer<ClassMapping<?, ?>> consumer) {
		consumer.accept(classMapping);

		for (InnerClassMapping innerClassMapping : classMapping.getInnerClassMappings()) {
			iterateClass(innerClassMapping, consumer);
		}
	}

	public static void failIfNotExisting(Path path) throws FileNotFoundException {
		if (Files.notExists(path)) {
			throw new FileNotFoundException(String.format("File at %s does not exist", path));
		}
	}

	public static MappingSet readTiny(Path path, String from, String to) throws IOException {
		try (FileReader fileReader = new FileReader(path.toFile())) {
			try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
				final TinyTree tinyTree = TinyMappingFactory.loadWithDetection(bufferedReader);

				if (!tinyTree.getMetadata().getNamespaces().contains(from)) {
					throw new RuntimeException(String.format("Could not find mapping namespace %s", from));
				}

				if (!tinyTree.getMetadata().getNamespaces().contains(to)) {
					throw new RuntimeException(String.format("Could not find mapping namespace %s", to));
				}

				return new TinyMappingsReader(tinyTree, from, to).read();
			}
		}
	}

	public static MappingSet readTsrg(Path path) throws IOException {
		try (final FileReader fileReader = new FileReader(path.toFile())) {
			try (BufferedReader reader = new BufferedReader(fileReader)) {
				return new TSrgReader(reader).read();
			}
		}
	}

	public static FieldTypeProvider typeProviderFromMappings(MappingSet mappings) {
		return fieldMapping -> mappings
				.getClassMapping(fieldMapping.getParent().getFullObfuscatedName())
				.flatMap(classMapping -> classMapping.getFieldMapping(fieldMapping.getObfuscatedName()))
				.flatMap(FieldMapping::getType);
	}
}
