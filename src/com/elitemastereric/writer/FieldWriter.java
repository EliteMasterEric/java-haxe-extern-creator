package com.elitemastereric.writer;

import java.io.IOException;
import java.io.OutputStream;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class FieldWriter extends BaseWriter {
    /**
     * Writes the given field, as Haxe, to the given stream.
     * 
     * @param stream  The stream to write to.
     * @param element The class to write.
     * @throws IOException
     */
    public static void writeField(OutputStream out, VariableElement element, int indent) throws IOException {
        boolean isPublic = false;
        boolean isPrivate = false;
        boolean isFinal = false;
        boolean isStatic = false;
        boolean isVolatile = false;

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
                case VOLATILE:
                    isVolatile = true;
                    break;
                default:
                    write(out, "~~~FIELDMODIFIER: %s%n", modifier);
            }
        }

        //
        // DON'T WRITE THE FIELD IF IT'S PRIVATE
        //
        if (isPrivate || !isPublic) {
            cancelQueue();
            return;
        }

        String fieldName = element.getSimpleName().toString();
        String typeName = TypeBuilder.buildType(element);

        DocumentationBuilder.writeDocs(out, element, indent);

        writeIndent(out, indent);
        write(out, "%s%s%s%s:%s;%n", isPublic ? "public " : "", isStatic ? "static " : "",
                isFinal ? "final " : "var ", fieldName, typeName);
    }
}
