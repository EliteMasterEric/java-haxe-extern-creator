package com.elitemastereric;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Utility methods for working with {@link Element} and {@link TypeElement}
 * objects.
 */
public class ElementUtils {
    public static boolean isPackage(Element element) {
        return element.getKind() == ElementKind.PACKAGE;
    }

    public static PackageElement getPackageElement(TypeElement element) {
        Element current = element;
        while (current != null && !isPackage(current)) {
            current = current.getEnclosingElement();
        }
        return (PackageElement) current;
    }

    public static VariableElement toVariableElement(Element element) {
        return (VariableElement) element;
    }

    public static <T extends Annotation> T[] getAnnotations(Element element, Class<T> annotationClass) {
        if (element == null) return (T[]) new Object[0];
        return element.getAnnotationsByType(annotationClass);
    }

    public static <T extends Annotation> T[] getAnnotations(TypeMirror type, Class<T> annotationClass) {
        if (type == null) return null;
        return type.getAnnotationsByType(annotationClass);
    }

    public static <T extends Annotation> int countAnnotations(Element element, Class<T> annotationClass) {
        if (element == null) return 0;
        return getAnnotations(element, annotationClass).length;
    }

    public static <T extends Annotation> int countAnnotations(TypeMirror type, Class<T> annotationClass) {
        if (type == null) return 0;
        return getAnnotations(type, annotationClass).length;
    }

    public static <T extends Annotation> boolean hasAnnotation(Element element, Class<T> annotationClass) {
        if (element == null) return false;
        return countAnnotations(element, annotationClass) > 0;
    }

    public static <T extends Annotation> boolean hasAnnotation(TypeMirror type, Class<T> annotationClass) {
        if (type == null) return false;
        return countAnnotations(type, annotationClass) > 0;
    }

    public static boolean isNullable(Element element) {
        return hasAnnotation(element, org.jetbrains.annotations.Nullable.class);
    }

    public static boolean isNullable(TypeMirror mirror) {
        return hasAnnotation(mirror, org.jetbrains.annotations.Nullable.class);
    }

    public static boolean isConstructor(ExecutableElement element) {
        return element.getKind() == ElementKind.CONSTRUCTOR;
    }

    public static String getPackageName(TypeElement element) {
        return getPackageElement(element).getQualifiedName().toString();
    }

    public static String escapeFormat(String format) {
        return format.replace("%", "%%");
    }
}
