package me.andrew28.addons.core;

/**
 * @author Andrew Tran
 */
public interface ExpressionManagerProvider {
    ExpressionManager getExpressionManager();

    default ExpressionManager exp() {
        return getExpressionManager();
    }

    default boolean isUsingExpressionManager() {
        return getExpressionManager() != null;
    }
}
