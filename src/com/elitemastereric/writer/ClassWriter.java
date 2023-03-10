package com.elitemastereric.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.elitemastereric.ElementUtils;

public class ClassWriter extends BaseWriter {
    /**
     * Writes the given class, as Haxe, to the given stream.
     * 
     * @param stream  The stream to write to.
     * @param element The class to write.
     * @throws IOException
     */
    public static void writeClass(OutputStream out, TypeElement element, int indent) throws IOException {
        writeInnerClass(out, element, "", indent);
    }

    public static void writeInnerClass(OutputStream out, TypeElement element, String prefix, int indent) throws IOException {
        String className = element.getSimpleName().toString();
        String prefixedClassName = prefix + element.getSimpleName();

        String packageName = ElementUtils.getPackageName(element);

        String qualifiedName = element.getQualifiedName().toString();
        String prefixedQualifiedName = qualifiedName.replace(element.getSimpleName(), prefixedClassName);

        boolean isPublic = false;
        boolean isPrivate = false;
        boolean isFinal = false;
        boolean isStatic = false;
        boolean isAbstract = false;

        for (Modifier modifier : element.getModifiers()) {
            switch (modifier) {
                case PUBLIC:
                    isPublic = true;
                    break;
                case PRIVATE:
                    isPrivate = true;
                    break;
                case PROTECTED:
                    isPrivate = true;
                    break;
                case FINAL:
                    isFinal = true;
                    break;
                case STATIC:
                    isStatic = true;
                    break;
                case ABSTRACT:
                    isAbstract = true;
                    break;
                default:
                    write(out, "~~~CLASSMODIFIER: %s%n", modifier);
            }
        }

        //
        // DON'T WRITE THE CLASS IF IT'S PRIVATE
        //
        if (isPrivate) {
            cancelQueue();
            return;
        }

        int elementNameIndex = 5;

        String typeParamList = "";

        for (TypeParameterElement typeParamElement : element.getTypeParameters()) {
            String elementName = TypeBuilder.buildType(typeParamElement.asType());

            if (typeParamElement.asType().getKind() == TypeKind.WILDCARD) {
                // Don't reuse element names.
                elementName = ELEMENT_NAMES[elementNameIndex++];
            }

            for (TypeMirror bound : typeParamElement.getBounds()) {
                String boundName = TypeBuilder.buildType(bound);
                if (boundName.equals("java.lang.Object")) {
                    continue;
                }
                elementName += ":" + boundName; // 'extends' in Java is ':' in Haxe, for generic constraints.
            }

            if (typeParamList.isBlank()) {
                typeParamList = elementName;
            } else {
                typeParamList += "," + elementName;
            }
        }

        if (indent == 0 && prefix == "") {
            write(out, "package %s;%n%n", packageName);
        } else {
            write(out, "%n");
        }

        DocumentationBuilder.writeDocs(out, element, indent);

        writeIndent(out, indent);
        write(out, "@:native('%s')%n", element.getQualifiedName());

        writeIndent(out, indent);
        write(out, "%s%sextern class %s%s%n", isAbstract ? "abstract " : "", isFinal ? "final " : "",
            prefixedClassName, typeParamList.isEmpty() ? "" : "<" + typeParamList + ">");
        
        writeIndent(out, indent);
        write(out, "{%n");

        boolean firstElement = true;
        ArrayList<TypeElement> externClasses = new ArrayList<TypeElement>();
        for (Element innerElement : element.getEnclosedElements()) {
            if (innerElement.getKind() == ElementKind.CLASS || innerElement.getKind() == ElementKind.RECORD || innerElement.getKind() == ElementKind.ENUM || innerElement.getKind() == ElementKind.INTERFACE) {
                externClasses.add((TypeElement) innerElement);
            } else {
                queueWrite(out, innerElement, indent);

                // Add extra line between elements
                if (firstElement) {
                    firstElement = false;
                } else {
                    queueWrite(out, "%n");
                }    
            }

            // Handle references to the inner type.
            replaceQueue(qualifiedName, prefixedQualifiedName);
            flushQueue(out);
        }

        writeIndent(out, indent);
        write(out, "}%n");

        for (TypeElement externClass : externClasses) {
            switch (externClass.getKind()) {
                case RECORD:
                    ClassWriter.writeInnerClass(out, externClass, prefixedClassName, indent);
                    break;
                case CLASS:
                    ClassWriter.writeInnerClass(out, externClass, prefixedClassName, indent);
                    break;
                case ENUM:
                    EnumWriter.writeInnerEnum(out, externClass, prefixedClassName, indent);
                    break;
                default:
                    write(out, "~~~INNERCLASS: %s%n", externClass.getKind());
            }
        }
    }
}
