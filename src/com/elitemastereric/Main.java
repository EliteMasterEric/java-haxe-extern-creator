package com.elitemastereric;

import java.io.File;
import java.util.ArrayList;

import javax.tools.DocumentationTool;
import javax.tools.DocumentationTool.DocumentationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

public class Main {
	public static void main(String[] args) {
		System.out.println("Java->Haxe Converter v2.0");

		String source = null;
		String packages = null;
		String output = null;
		String classpath = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-source")) {
				if (i > args.length - 2) {
					throw new IllegalArgumentException("-source requires a source directory to be specified");
				}
				source = args[++i];
			} else if (args[i].equals("-output")) {
				if (i > args.length - 2) {
					throw new IllegalArgumentException("-output requires an output directory to be specified");
				}
				output = args[++i];
			} else if (args[i].equals("-packages")) {
				if (i > args.length - 2) {
					throw new IllegalArgumentException("-packages requires a list of packages, seperated by colons");
				}
				packages = args[++i];
			} else if (args[i].equals("-classpath")) {
				if (i > args.length - 2) {
					throw new IllegalArgumentException("-classpath requires a classpath directory to be specified");
				}
				classpath = args[++i];
			}
		}
		if (source == null || packages == null || output == null || classpath == null) {
			throw new IllegalArgumentException(
					"Usage: -source (source-dir) -output (output-dir) -packages (package-list) -classpath (classpath-dir)");
		}

		runDoclet(source, output, packages, classpath);
	}

	/**
	 * Computes the final arguments for the DocumentationTool and runs it.
	 */
	private static void runDoclet(String source, String output, String packages, String classpath) {
		ArrayList<String> options = new ArrayList<>();
		// List<File> sourceFiles = findSourceFiles(packages, source);
		ArrayList<File> sourceFiles = new ArrayList<>();

		options.add("-sourcepath");
		options.add(source);
		options.add("-subpackages");
		options.add(packages);
		options.add("-classpath");
		options.add(classpath);
		options.add("-Xmaxerrs");
		options.add("100000");
		options.add("-outputdir");
		options.add(output);

		DocumentationTool tool = javax.tools.ToolProvider.getSystemDocumentationTool();
		StandardJavaFileManager fileManager = tool.getStandardFileManager(null, null, null);
		Iterable<? extends JavaFileObject> fileObjects = fileManager
				.getJavaFileObjects(sourceFiles.toArray(new File[0]));

		DocumentationTask task = tool.getTask(
				null, // default writer (System.err)
				null, // default file manager
				null, // default diagnostic listener
				HaxeDoclet.class, // default doclet class
				options,
				fileObjects);

		task.call();
	}
}
