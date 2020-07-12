import java.io.IOException;
import java.io.Writer;

import org.cadixdev.lorenz.MappingSet;
import org.cadixdev.lorenz.io.TextMappingsWriter;
import org.cadixdev.lorenz.model.FieldMapping;
import org.cadixdev.lorenz.model.MethodMapping;

public class TinyV2MappingsWriter extends TextMappingsWriter {
	private final String obfNamespace;
	private final String deobfNamespace;

	/**
	 * Creates a new mappings writer, from the given {@link Writer}.
	 *
	 * @param writer The output writer, to write to
	 */
	public TinyV2MappingsWriter(Writer writer, String obfNamespace, String deobfNamespace) {
		super(writer);
		this.obfNamespace = obfNamespace;
		this.deobfNamespace = deobfNamespace;
	}

	@Override
	public void write(MappingSet mappings) throws IOException {
		// Write the header, below for example
		// tiny	2	0	official	intermediary

		this.writer.print("tiny");
		this.printTab();
		this.writer.print("2");
		this.printTab();
		this.writer.print("0");
		this.printTab();
		this.writer.print(this.deobfNamespace);
		this.printTab();
		this.writer.print(this.obfNamespace);

		// Write our first class entry
		MappingUtils.iterateClasses(mappings, classMapping -> {
			this.writer.println(); // Always NL before next class

			this.writer.print("c"); // Class
			this.printTab();
			this.writer.print(classMapping.getFullObfuscatedName()); // Obf
			this.printTab();
			this.writer.print(classMapping.getFullDeobfuscatedName()); // Deobf

			// Methods
			for (MethodMapping methodMapping : classMapping.getMethodMappings()) {
				this.writer.println(); // Newline for method
				this.printTab(); // Indent by 1
				this.writer.print("m"); // Method
				this.printTab();
				this.writer.print(methodMapping.getSignature().getDescriptor().toString()); // Method descriptor
				this.printTab();
				this.writer.print(methodMapping.getSimpleObfuscatedName()); // Obf
				this.printTab();
				this.writer.print(methodMapping.getSimpleDeobfuscatedName()); // Deobf
			}

			for (FieldMapping fieldMapping : classMapping.getFieldMappings()) {
				this.writer.println(); // Newline for field
				this.printTab(); // Ident by 1
				this.writer.print("f"); // Method
				this.printTab();

				// Write the field type. This is required
				this.writer.print(fieldMapping.getType().orElseThrow(() -> {
					return new RuntimeException(String.format("Cannot write field \"%s -> %s\" in class \"%s -> %s\" since it has no field type",
							fieldMapping.getObfuscatedName(), fieldMapping.getDeobfuscatedSignature(), classMapping.getFullObfuscatedName(), classMapping.getFullDeobfuscatedName()));
				}));

				this.printTab();
				this.writer.print(fieldMapping.getObfuscatedName()); // Obf
				this.printTab();
				this.writer.print(fieldMapping.getDeobfuscatedName()); // Deobf
			}
		});
	}

	private void printTab() {
		this.writer.print("\t");
	}
}
