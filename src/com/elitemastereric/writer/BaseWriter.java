package com.elitemastereric.writer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import com.elitemastereric.ElementUtils;

public class BaseWriter {
    public static final String[] ELEMENT_NAMES = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
    /**
     * Some variables were given names in Java that are reserved in Haxe.
     */
    public static final List<String> RESERVED_NAMES = List.of("cast");
    static final String INDENT = "  ";

    /**
     * If an output directory is specified, this will be the output stream to write
     * to. Otherwise,
     * print to System.out.
     */
    public static OutputStream buildOutputStream(String outputDir, TypeElement element) {
        if (outputDir == null) {
            return System.out;
        } else {
            File output = createHaxeFile(outputDir, element);
            try {
                return new BufferedOutputStream(new FileOutputStream(output));
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public static OutputStream cleanupOutputStream(OutputStream out) {
        if (out != System.out) {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return out;
    }

    /**
     * Writes an Element to the output stream based on its type.
     * @param out The output stream to write to.
     * @param element The element to write.
     * @param docTrees The documentation trees.
     * 
     * @throws IOException If an error occurs while writing to the output stream.
     * @boolean True if the element was written, false otherwise.
     */
    public static void write(OutputStream out, Element element, int indent) throws IOException {
        if (element == null) return;

        // The VISITOR design pattern is really dumb just use a switch statement
        switch (element.getKind()) {
            case CLASS:
                ClassWriter.writeClass(out, (TypeElement) element, indent + 1);
                break;
            case RECORD:
                ClassWriter.writeClass(out, (TypeElement) element, indent + 1);
                break;
            case FIELD:
                // write(out, "FIELD: ");
                FieldWriter.writeField(out, (VariableElement) element, indent + 1);
                break;
            case CONSTRUCTOR:
            case METHOD:
                ExecutableWriter.writeExecutable(out, (ExecutableElement) element, indent + 1);
                break;
            case INTERFACE:
                InterfaceWriter.writeInterface(out, (TypeElement) element, indent + 1);
                break;
            case ENUM:
                EnumWriter.writeEnum(out, (TypeElement) element, indent + 1);
                break;
            case ENUM_CONSTANT:
                EnumWriter.writeEnumConstant(out, (VariableElement) element, indent + 1);
                break;
            case RECORD_COMPONENT:
                RecordComponentWriter.writeRecordComponent(out, (RecordComponentElement) element, indent + 1);
                break;
            default:
                // Write an easily searchable string to the output stream.
                write(out, "~~~ELEMENT: %s%n", element.getKind());
                break;
        }
    }

    public static void write(OutputStream out, Element element) throws IOException {
        // Class is at 0 indent so root element is at -1 indent
        write(out, element, -1);
    }

    public static void writeIndent(OutputStream out, int indent) throws IOException {
        if (indent <= 0) return;

        for (int i = 0; i < indent; i++) {
            write(out, INDENT);
        }
    }

    static File createHaxeFile(String outputDir, TypeElement element) {
        String elementPackage = ElementUtils.getPackageName(element);

        String packagePath = elementPackage.replace('.', File.separatorChar);

        File output = new File(outputDir, packagePath);
        output.mkdirs();
        if (!output.exists()) {
            throw new RuntimeException("Could not create required directory in output directory");
        }
        output = new File(output, element.getSimpleName().toString() + ".hx");

        return output;
    }

    static void write(OutputStream out, String format, Object... args) throws IOException {
        if (format == null || format.isEmpty()) return;

        if (format == "%n" && isLastWriteQueueNewLine()) return;

        flushQueue(out);

        out.write(String.format(format, args).toString().getBytes());
    }

    /**
     * A set 
     */
    private static ArrayList<String> writeQueue = new ArrayList<String>();

    public static void queueWrite(OutputStream out, String format, Object... args) throws IOException {
        if (format == null) return;

        if (format == "%n" && isLastWriteQueueNewLine()) return;

        writeQueue.add(String.format(format, args));
    }

    public static void queueWrite(OutputStream out, Element element, int indent) throws IOException {
        if (element == null) return;

        StringOutputStream sos = new StringOutputStream();
        write(sos, element, indent);

        writeQueue.add(sos.toString());
    }

    public static void replaceQueue(String find, String replace) {
        for (int i = 0; i < writeQueue.size(); i++) {
            String s = writeQueue.get(i);
            writeQueue.set(i, s.replace(find, replace));
        }
    }

    static boolean isLastWriteQueueNewLine() {
        if (writeQueue.isEmpty()) return false;

        String s = writeQueue.get(writeQueue.size() - 1);
        return s.endsWith(String.format("%n"));
    }

    public static void flushQueue(OutputStream out) throws IOException {
        if (writeQueue.isEmpty()) return;

        for (String s : writeQueue) {
            out.write(s.getBytes());
        }
        writeQueue.clear();
    }

    public static void cancelQueue() {
        writeQueue.clear();
    }
}
