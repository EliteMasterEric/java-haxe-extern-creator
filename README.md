# Java Haxe Extern Generator

Parses a folder of `.java` files into appropriate `.hx` externs!

## Note for Users

This generator is typically not necessary. The compiler argument `--java-lib-extern` 

## Usage

Given:
- a folder of `.java` files in `./source-folder/`
- a set of packages to parse `net.package.a`, `net.package.b`
- a set of jar files for the `.java` dependencies
- a desired output folder at `./output-folder/` (optional)

Usage is simple:

```bash
java -jar java-haxe-extern-creator.jar -source ./source-folder/ -output ./output-folder/ --packages net.package.a:net.package.b -classpath ./1.jar;./2.jar
```

This will create matching Haxe externs in (output-dir) for each Java class in the specified packages, looking in (source-dir).

The result will be structured based on the class hierarchy of the Java files. The result will include proper types, and proper HaxeDox (converted from JavaDox).

If output directory is not specified, output will be displayed in the console.

Before utilizing the generated Haxe externs, check for any instances of `~~~` in the output; these represent an unhandled case. Report these in the Issues tab on the Github page.

## TODO List

- [X] Fix `Null<@org.jetbrains.annotations.Nullable Type>`
- [X] Resolve unknown modifier: `protected`
- [X] Resolve unknown modifier: `abstract`
- [X] Fix bare javadoc comment (ArmorItem#getSlot)
- [X] Fix javadoc comment with @code (Potion#byName)
- [X] Fix javadoc comment with param and return (Potion#byName)
- [X] Resolve element: `interface`
- [X] Resolve element: `enum`
- [X] Resolve type: Wildcard (`?`)
- [X] Resolve all documentation
- [] Resolve element: `record`
- [] Resolve element: `annotation_type`
