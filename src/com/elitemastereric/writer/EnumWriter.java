package com.elitemastereric.writer;

import java.io.IOException;
import java.io.OutputStream;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.elitemastereric.ElementUtils;

public class EnumWriter extends BaseWriter {
    /**
     * Writes the given enum, as Haxe, to the given stream.
     * 
     * @param stream  The stream to write to.
     * @param element The enum to write.
     * @throws IOException
     */
    public static void writeEnum(OutputStream out, TypeElement element, int indent) throws IOException {
        writeInnerEnum(out, element, "", indent);
    }

    public static void writeInnerEnum(OutputStream out, TypeElement element, String prefix, int indent) throws IOException {
        String packageName = ElementUtils.getPackageName(element);

        String enumName = element.getSimpleName().toString();
        String prefixedEnumName = prefix + enumName;

        boolean isPublic = false;
        boolean isPrivate = false;
        boolean isFinal = false;
        boolean isStatic = false;
        boolean isAbstract = false;
        boolean isSealed = false;

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
                case SEALED:
                    isSealed = true;
                    break;
                default:
                    write(out, "~~~ENUMMODIFIER: %s%n", modifier);
            }
        }

        //
        // DON'T WRITE THE INTERFACE IF IT'S PRIVATE
        //
        if (isPrivate) {
            cancelQueue();
            return;
        }

        int elementNameIndex = 10;

        String typeParamList = "";

        for (TypeParameterElement typeParamElement : element.getTypeParameters()) {
            String elementName = TypeBuilder.buildType(typeParamElement.asType());

            if (typeParamElement.asType().getKind() == TypeKind.WILDCARD) {
                // Don't reuse element names.
                elementName = BaseWriter.ELEMENT_NAMES[elementNameIndex++];
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

        if (indent == 0 && packageName.isBlank()) {
            write(out, "package %s;%n%n", packageName);
        }

        DocumentationBuilder.writeDocs(out, element, indent);

        writeIndent(out, indent);
        write(out, "@:native('%s')%n", element.getQualifiedName());

        writeIndent(out, indent);
        write(out, "%s%sextern class %s extends java.lang.Enum<%s>%n", isAbstract ? "abstract " : "", isFinal ? "final " : "",
                prefixedEnumName, element.getQualifiedName());
        
        writeIndent(out, indent);
        write(out, "{%n");

        boolean firstElement = true;
        for (Element innerElement : element.getEnclosedElements()) {
            write(out, innerElement, indent);

            // Add extra line between elements
            if (firstElement) {
                firstElement = false;
            } else {
                queueWrite(out, "%n");
            }
        }

        writeIndent(out, indent);
        write(out, "}%n");
    }

    /**
     * Writes the given enum constant, as Haxe, to the given stream.
     */
    public static void writeEnumConstant(OutputStream out, VariableElement element, int indent) throws IOException {
        DocumentationBuilder.writeDocs(out, element, indent);

        writeIndent(out, indent);
        write(out, "public static var %s:%s;%n", element.getSimpleName(), TypeBuilder.buildType(element.asType()));
    }
}
