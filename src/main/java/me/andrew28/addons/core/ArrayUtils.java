package me.andrew28.addons.core;

/**
 * @author Andrew Tran
 */
public class ArrayUtils {
    public static boolean indexExists(final Object[] arr, final int index) {
        return index >= 0 && index < arr.length;
    }
}
