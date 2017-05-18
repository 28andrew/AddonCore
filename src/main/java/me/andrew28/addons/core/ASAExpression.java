package me.andrew28.addons.core;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import me.andrew28.addons.core.annotations.DoNotRegister;
import org.bukkit.event.Event;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author Andrew Tran
 */
@DoNotRegister
public abstract class ASAExpression<T> extends SimpleExpression<T> implements AutoRegisteringSkriptElement, ExpressionManagerProvider {
    private Class<T> expressionType;
    private ExpressionManager expressionManager = new ExpressionManager();

    private ElementSyntax[] elementSyntaxes;
    private boolean usesBinds = false;

    private Event currentEvent;
    private Boolean single;

    private ASAChanger[] asaChangers;

    private SkriptParser.ParseResult parseResult;

    public SkriptParser.ParseResult getParseResult() {
        return parseResult;
    }

    public ElementSyntax[] getElementSyntaxes() {
        return elementSyntaxes;
    }

    @Override
    protected T[] get(Event event) {
        exp().setCurrentEvent(event);
        setCurrentEvent(event);
        try {
            return isSingle() ? (T[]) new Object[]{getValue()} : getValues();
        } catch (NullExpressionException e) {
            Skript.warning("Unexpected null value given to Expression " + getClass().getCanonicalName() + " (Message: " + e.getMessage() + ")");
        }
        return null;
    }

    @Override
    public boolean isSingle() {
        return single;
    }

    public void setSingle(Boolean single) {
        this.single = single;
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
    }

    public Class<? extends Event>[] getRequiredEvents() {
        return null;
    }

    @Override
    public Class<? extends T> getReturnType() {
        if (expressionType == null) {
            // TODO : Make less hacky
            expressionType = (Class<T>) ((ParameterizedType) getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0];
        }
        return expressionType;
    }

    @Override
    public ExpressionManager getExpressionManager() {
        return expressionManager;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedElementSyntax, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        elementSyntaxes = SyntaxElementUtil.getSyntax(getClass());
        this.parseResult = parseResult;

        if (getRequiredEvents() != null) {
            if (!ScriptLoader.isCurrentEvent(getRequiredEvents())) {
                Skript.error("This expression can only be used in the event[s]: " + String.join(", ", Arrays.stream(getRequiredEvents()).map(Class::getSimpleName).toArray(String[]::new)));
                return false;
            }
        }

        try {
            single = getClass().getMethod("getValue", null).getDeclaringClass().equals(getClass());
            if (!single) {
                if (!getClass().getMethod("getValues", null).getDeclaringClass().equals(getClass())) {
                    throw new IllegalStateException("Neither of the methods getValue or getValues were implemented.");
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        for (ElementSyntax elementSyntax : elementSyntaxes) {
            if (elementSyntax != null && elementSyntax.isUsingBinds()) {
                usesBinds = true;
                break;
            }
        }
        if (usesBinds) {
            List<ElementSyntax> elementSyntaxesForBinds = new ArrayList<>();
            for (ElementSyntax elementSyntax : elementSyntaxes) {
                for (int i = 0; i < elementSyntax.getRawSyntaxes().length; i++) {
                    elementSyntaxesForBinds.add(elementSyntax);
                }
            }
            ElementSyntax matchedBindedSyntax = elementSyntaxesForBinds.get(matchedElementSyntax);
            if (matchedBindedSyntax.isUsingBinds()) {
                getExpressionManager().bind(matchedBindedSyntax, expressions);
            }
        }
        SkriptExpressionWrapper<?>[] skriptExpressionWrappers = Arrays.stream(expressions)
                .map((Function<Expression<?>, ? extends SkriptExpressionWrapper<?>>) SkriptExpressionWrapper::new)
                .toArray(SkriptExpressionWrapper[]::new);
        return init(skriptExpressionWrappers, matchedElementSyntax, kleenean, parseResult);
    }

    public boolean init(SkriptExpressionWrapper<?>[] expressions, int matchedPattern, Kleenean delay, SkriptParser.ParseResult parseResult) {
        return true;
    }

    public ASAChanger[] getChangers() {
        return null;
    }

    @Override
    public Class<?>[] acceptChange(Changer.ChangeMode mode) {
        asaChangers = getChangers();
        if (asaChangers == null) {
            return null;
        }
        for (ASAChanger asaChanger : asaChangers) {
            for (Changer.ChangeMode changeMode : asaChanger.getChangeModes()) {
                if (changeMode.equals(mode)) {
                    return new Class[]{asaChanger.getDeltaType()};
                }
            }
        }
        return null;
    }

    @Override
    public void change(Event e, Object[] delta, Changer.ChangeMode mode) {
        exp().setCurrentEvent(e);
        setCurrentEvent(e);
        for (ASAChanger asaChanger : asaChangers) {
            for (Changer.ChangeMode changeMode : asaChanger.getChangeModes()) {
                if (changeMode.equals(mode)) {
                    try {
                        asaChanger.change(delta);
                    } catch (NullExpressionException e1) {
                        Skript.warning("Unexpected null value given to Changer (Mode: " + mode.name() + ") for Expression " + getClass().getCanonicalName() + " (Message: " + e1.getMessage() + ")");
                    }
                    break;
                }
            }
        }
    }

    @Override
    public String toString(Event event, boolean b) {
        return toString();
    }

    @Override
    public String toString() {
        return "Andrew's Addon System Expression: " + getClass().getCanonicalName();
    }

    public T getValue() throws NullExpressionException {
        return null;
    }

    public T[] getValues() throws NullExpressionException {
        return null;
    }

    public void assertNotNull(Object object, String message) throws NullExpressionException {
        if (object == null) {
            throw new NullExpressionException(message);
        }
    }
}
