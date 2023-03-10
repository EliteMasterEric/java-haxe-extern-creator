package com.elitemastereric.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;

import com.elitemastereric.ElementUtils;

public class ExecutableWriter extends BaseWriter {
    static final String CONSTRUCTOR_NAME = "new";

    /**
     * Writes the given field, as Haxe, to the given stream.
     * 
     * @param stream  The stream to write to.
     * @param element The class to write.
     * @throws IOException
     */
    public static void writeExecutable(OutputStream out, ExecutableElement element, int indent)
            throws IOException {
        boolean isPublic = false;
        boolean isPrivate = false;
        boolean isFinal = false;
        boolean isStatic = false;
        boolean isAbstract = false;
        boolean isDefault = false;
        boolean isSynchronized = false;

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
                case DEFAULT:
                    isDefault = true;
                    break;
                case SYNCHRONIZED:
                    isSynchronized = true;
                    break;
                default:
                    write(out, "~~~EXECUTABLEMODIFIER: %s%n", modifier);
            }
        }

        //
        // DON'T WRITE THE FIELD IF IT'S PRIVATE
        //
        if (isPrivate) {
            cancelQueue();
            return;
        }
        
        String fieldName = element.getSimpleName().toString();
        
        boolean isConstructor = ElementUtils.isConstructor(element);
        if (isConstructor) {
            fieldName = CONSTRUCTOR_NAME;
            isPublic = true; // ???
        }

        String returnType = "";
                
        if (ElementUtils.isNullable(element)) {
            returnType = String.format(":Null<%s>", TypeBuilder.buildType(element.getReturnType()));
        } else if (!isConstructor) {
            returnType = String.format(":%s", TypeBuilder.buildType(element.getReturnType()));
        }
        // else keep it empty

        String parameterList = "";
        int elementNameIndex = 0;
        for (VariableElement parameter : element.getParameters()) {
            String parameterName = parameter.getSimpleName().toString();
            if (parameterName.isEmpty()) {
                parameterName = String.format("arg%d", elementNameIndex);
            }
            String parameterType = TypeBuilder.buildType(parameter.asType());
            if (ElementUtils.isNullable(parameter)) {
                parameterType = String.format("Null<%s>", parameterType);
            }
            parameterList += String.format("%s:%s", parameterName, parameterType);
            if (elementNameIndex < element.getParameters().size() - 1) {
                parameterList += ", ";
            }
            elementNameIndex++;
        }

        DocumentationBuilder.writeDocs(out, element, indent);
        
        if (BaseWriter.RESERVED_NAMES.contains(fieldName)) {
            fieldName = String.format("do%s", fieldName);

            writeIndent(out, indent);
            write(out, "@:native('%s')%n", element.getSimpleName());
        }
        
        writeIndent(out, indent);
        write(out, "%s%s%sfunction %s(%s)%s;%n", isPublic ? "public " : "", isStatic ? "static " : "",
                isFinal ? "final " : "",
                fieldName, parameterList == null ? "" : parameterList, returnType);
    }
}
