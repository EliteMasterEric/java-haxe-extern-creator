package com.elitemastereric;

import java.util.List;

import jdk.javadoc.doclet.Doclet;

/**
 * A command line option that can be passed to the HaxeDoclet.
 */
public abstract class HaxeDocletOption implements Doclet.Option {
    private final String name;
    private final boolean hasArg;
    private final String description;
    private final String parameters;

    public HaxeDocletOption(String name, boolean hasArg, String description, String parameters) {
        this.name = name;
        this.hasArg = hasArg;
        this.description = description;
        this.parameters = parameters;
    }

    @Override
    public int getArgumentCount() {
        return hasArg ? 1 : 0;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getParameters() {
        return parameters;
    }

    @Override
    public List<String> getNames() {
        return List.of(name);
    }

    @Override
    public Kind getKind() {
        return Kind.STANDARD;
    }
}
