package com.elitemastereric.writer;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class ElementBuilder implements ElementVisitor<String, Void> {

    public static final ElementBuilder INSTANCE = new ElementBuilder();

    public static String buildElement(Element e) {
        return e.accept(INSTANCE, null);
    }

    @Override
    public String visit(Element e, Void _v) {
        return e.accept(this, null);
    }

    @Override
    public String visitPackage(PackageElement e, Void _v) {
        return "~~~PACKAGEELEMENT: " + e.getQualifiedName();
    }

    @Override
    public String visitType(TypeElement e, Void _v) {
        return "~~~TYPEELEMENT: " + e.getSimpleName() + "~" + e.getQualifiedName();
    }

    @Override
    public String visitVariable(VariableElement e, Void _v) {
        return String.format("%s:%s", e.getSimpleName(), TypeBuilder.buildType(e.asType()));
    }

    @Override
    public String visitExecutable(ExecutableElement e, Void _v) {
        return "~~~EXECUTABLEELEMENT: " + e.getSimpleName();
    }

    @Override
    public String visitTypeParameter(TypeParameterElement e, Void _v) {
        return "~~~TYPEPARAMETERELEMENT: " + e.getSimpleName();
    }

    @Override
    public String visitUnknown(Element e, Void _v) {
        return "~~~UNKNOWNELEMENT: " + e.toString();
    }

    public static TypeMirror asType(Element e) {
        return e.asType();
    }
}
