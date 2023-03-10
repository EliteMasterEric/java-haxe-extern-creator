package com.elitemastereric;

import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.elitemastereric.writer.BaseWriter;
import com.elitemastereric.writer.DocumentationBuilder;
import com.elitemastereric.writer.TypeBuilder;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/**
 * A Doclet that generates Haxe code from Java code.
 */
public class HaxeDoclet implements Doclet {
    Reporter reporter;

    String outputDir = null;

    @Override
    public void init(Locale locale, Reporter reporter) {
        System.out.println("Initializing doclet...");
        this.reporter = reporter;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // Doesn't support language features newer than Java 17.
        return SourceVersion.RELEASE_17;
    }

    private final Set<HaxeDocletOption> options = Set.of(
            new HaxeDocletOption("-outputdir", true, "The directory to output Haxe files to.", "outputDir") {
                @Override
                public boolean process(String value, List<String> arguments) {
                    if (arguments.size() < 1) {
                        return false;
                    }

                    outputDir = arguments.get(0);

                    return true;
                }
            });

    @Override
    public Set<HaxeDocletOption> getSupportedOptions() {
        // This doclet does not support any options.
        return options;
    }

    @Override
    public boolean run(DocletEnvironment environment) {
        System.out.println("Running doclet...");

        // Tell the user if we are printing to a file or the console.
        System.out.println(outputDir != null
                ? "Outputting to: " + outputDir
                : "Outputting to System.out");

        // Retrieve the list of classes and interfaces we are processing.
        Set<TypeElement> includedElements = environment.getIncludedElements().stream()
                .filter(e -> e instanceof TypeElement && environment.isIncluded(e)) // Filter to types.
                .map(e -> (TypeElement) e).collect(Collectors.toSet()); // Convert back to set.
        
        // Retrieve the documentation trees for the classes and interfaces we are processing.
        DocumentationBuilder.INSTANCE.setDocTrees(environment.getDocTrees());
        TypeBuilder.INSTANCE.setTypeUtils(environment.getTypeUtils());

        // Tell the user how many classes and interfaces we are processing.
        System.out.println(
                "Processing " + includedElements.size() + " included elements (class, enum, interface, or record).");

        // For each type element, write the Haxe code to the output stream (System.out or a FileOutputStream).
        for (TypeElement element : includedElements) {
            OutputStream out = BaseWriter.buildOutputStream(outputDir, element);
            try {
                BaseWriter.write(out, element);
            } catch (Exception e) {
                reporter.print(Diagnostic.Kind.ERROR, element, "Error writing element: " + element.toString());
                System.out.println(e);
            } finally {
                BaseWriter.cleanupOutputStream(out);
            }
        }

        return true;
    }
}
