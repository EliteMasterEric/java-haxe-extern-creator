package com.elitemastereric.writer;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;

import com.elitemastereric.ElementUtils;

/**
 * Utilities for retrieving the name of a Type element.
 */
public class TypeBuilder implements TypeVisitor<String, Void> {

    public static final TypeBuilder INSTANCE = new TypeBuilder();

    private Types typeUtils = null;

    public Types getTypeUtils() {
        if (typeUtils == null)
            throw new IllegalStateException("TypeUtils not set");
        return typeUtils;
    }

    public void setTypeUtils(Types typeUtils) {
        this.typeUtils = typeUtils;
    }

    public static String buildType(TypeMirror t) {
        if (t == null) return null;
        return t.accept(INSTANCE, null);
    }

    public static String buildType(Element e) {
        if (e == null) return null;
        return buildType(e.asType());
    }

    @Override
    public String visit(TypeMirror t, Void _v) {
        return t.accept(this, null);
    }

    @Override
    public String visitPrimitive(PrimitiveType t, Void _v) {

        switch (t.getKind()) {
            case BOOLEAN:
                return "Bool";
            case BYTE:
                return "Int";
            case SHORT:
                return "Int";
            case INT:
                return "Int";
            case LONG:
                return "Int";
            case CHAR:
                return "Int";
            case FLOAT:
                return "Float";
            case DOUBLE:
                return "Float";
            default:
                return "~~~PRIMATIVE:" + t.getKind();
        }
    }

    @Override
    public String visitNull(NullType t, Void _v) {
        return "~~~NULLTYPE:" + t.toString();
    }

    @Override
    public String visitArray(ArrayType t, Void _v) {
        if (ElementUtils.isNullable(t))
            return String.format("Null<%s>", visitArray(t, null));

        return String.format("Array<%s>", visit(t.getComponentType(), null));
    }

    @Override
    public String visitDeclared(DeclaredType t, Void _v) {
        if (ElementUtils.isNullable(t))
            return String.format("Null<%s>", t.asElement().toString());

        if (t.getTypeArguments() != null && !t.getTypeArguments().isEmpty()) {
            String typeParamList = "";
            int elementNameIndex = 0;
    
            for (TypeMirror type : t.getTypeArguments()) {
                if (type == null) continue;

                String elementName = TypeBuilder.buildType(type);
    
                if (type.getKind() == TypeKind.WILDCARD) {
                    // Don't reuse element names.
                    // elementName = BaseWriter.ELEMENT_NAMES[elementNameIndex++];

                    WildcardType wildcard = (WildcardType) type;

                    TypeMirror bound = wildcard.getExtendsBound();
                    if (bound == null) {
                        continue;
                    }
                    String boundName = TypeBuilder.buildType(bound);
                    if (boundName.equals("java.lang.Object")) {
                        continue;
                    }
                    // elementName += ":" + boundName;
                    elementName = boundName;
                }
    
                if (typeParamList.isBlank()) {
                    typeParamList = elementName;
                } else {
                    typeParamList += "," + elementName;
                }
            }
        
            if (typeParamList.isBlank()) typeParamList = "Dynamic";

            return String.format("%s<%s>", t.asElement().toString(), typeParamList);
        }

        return t.asElement().toString();
    }

    @Override
    public String visitError(ErrorType t, Void _v) {
        return "~~~ERRORTYPE:" + t.toString();
    }

    @Override
    public String visitTypeVariable(TypeVariable t, Void _v) {
        // A parameterized type, such as T.
        return t.toString();
    }

    @Override
    public String visitWildcard(WildcardType t, Void _v) {
        return "~~~WILDCARDTYPE:" + t.toString();
    }

    @Override
    public String visitExecutable(ExecutableType t, Void _v) {
        return "~~~EXECUTABLETYPE:" + t.toString();
    }

    @Override
    public String visitNoType(NoType t, Void _v) {
        return "Void";
    }

    @Override
    public String visitUnknown(TypeMirror t, Void _v) {
        return "~~~UNKNOWNTYPE:" + t.toString();
    }

    @Override
    public String visitUnion(UnionType t, Void _v) {
        return "~~~UNIONTYPE:" + t.toString();
    }

    @Override
    public String visitIntersection(IntersectionType t, Void _v) {
        return "~~~INTERSECTIONTYPE:" + t.toString();
    }

    public static Element asElement(TypeMirror type) {
        return INSTANCE.getTypeUtils().asElement(type);
    }

    public static DeclaredType getDeclaredType(TypeElement element) {
        return INSTANCE.getTypeUtils().getDeclaredType(element);
    }
    
    /**
     * Haxe formats variables (such as method parameters) as "name:type" (e.g.
     * "foo:Int"
     */
    // static String parseVariable(VariableElement element) {
    //     String name = element.getSimpleName().toString();
    //     String type = parseType(element);
    //     return String.format("%s:%s", name, type);
    // }
}
