package me.andrew28.addons.core;

/**
 * @author Andrew Tran
 */
public class ASAElement {
    private String name, description;
    private ElementType elementType;
    private ElementSyntax[] elementSyntaxes;
    private Class<? extends AutoRegisteringSkriptElement> elementClass;
    private Boolean document;
    private String[][] examples;

    public ASAElement(String name, String description, ElementType elementType, ElementSyntax[] elementSyntaxes, Class<? extends AutoRegisteringSkriptElement> elementClass, Boolean document, String[][] examples) {
        this.name = name;
        this.description = description;
        this.elementType = elementType;
        this.elementSyntaxes = elementSyntaxes;
        this.elementClass = elementClass;
        this.document = document;
        this.examples = examples;
    }

    public String[][] getExamples() {
        return examples;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public ElementSyntax[] getElementSyntaxes() {
        return elementSyntaxes;
    }

    public Class<? extends AutoRegisteringSkriptElement> getElementClass() {
        return elementClass;
    }

    public Boolean shouldDocument() {
        return document;
    }

    public static enum ElementType {
        CONDITION(ASACondition.class),
        EFFECT(ASAEffect.class),
        EVENT(ASAEvent.class),
        PROPERTY_EXPRESSION(ASAPropertyExpression.class),
        EXPRESSION(ASAExpression.class),
        TYPE(ASAType.class);
        Class<? extends AutoRegisteringSkriptElement> elementClass;

        ElementType(Class<? extends AutoRegisteringSkriptElement> elementClass) {
            this.elementClass = elementClass;
        }

        public Class<? extends AutoRegisteringSkriptElement> getElementClass() {
            return elementClass;
        }
    }
}
