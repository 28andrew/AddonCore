package me.andrew28.addons.core;

import me.andrew28.addons.core.annotations.BSyntax;
import me.andrew28.addons.core.annotations.BSyntaxes;
import me.andrew28.addons.core.annotations.Syntax;
import me.andrew28.addons.core.annotations.Syntaxes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Tran
 */
public class SyntaxElementUtil {
    private static Map<String, ElementSyntax[]> syntaxes = new HashMap<>();

    public static ElementSyntax[] getSyntax(Class<?> cls) {
        if (!syntaxes.containsKey(cls.getCanonicalName())) {
            List<ElementSyntax> elementSyntaxes = new ArrayList<>();
            if (cls.isAnnotationPresent(BSyntaxes.class)) {
                BSyntaxes bSyntaxes = cls.getAnnotation(BSyntaxes.class);
                for (BSyntax bSyntax : bSyntaxes.value()) {
                    ElementSyntax elementSyntax = new ElementSyntax(true, bSyntax.syntax());
                    elementSyntax.setBinds(bSyntax.bind());
                    elementSyntaxes.add(elementSyntax);
                }
            }
            if (cls.isAnnotationPresent(BSyntax.class)) {
                BSyntax bSyntax = cls.getAnnotation(BSyntax.class);
                ElementSyntax elementSyntax = new ElementSyntax(true, bSyntax.syntax());
                elementSyntax.setBinds(bSyntax.bind());
                elementSyntaxes.add(elementSyntax);
            }
            if (cls.isAnnotationPresent(Syntaxes.class)) {
                Syntaxes syntaxes = cls.getAnnotation(Syntaxes.class);
                for (Syntax syntax : syntaxes.value()) {
                    elementSyntaxes.add(new ElementSyntax(false, syntax.value()));
                }
            }
            if (cls.isAnnotationPresent(Syntax.class)) {
                Syntax syntax = cls.getAnnotation(Syntax.class);
                elementSyntaxes.add(new ElementSyntax(false, syntax.value()));
            }
            syntaxes.put(cls.getCanonicalName(), elementSyntaxes.toArray(new ElementSyntax[elementSyntaxes.size()]));
        }
        return syntaxes.get(cls.getCanonicalName());
    }
}
