package io.github.gaming32.nullabilitysyntax;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

public class Utils {
    public static <T> T[] addAfter(T[] array, T after, T value) {
        return add(array, indexOf(array, after) + 1, value);
    }

    public static <T> T[] add(T[] array, T value) {
        final T[] result = Arrays.copyOf(array, array.length + 1);
        result[array.length] = value;
        return result;
    }

    public static <T> T[] add(T[] array, int index, T value) {
        if (index == array.length) {
            return add(array, value);
        }
        @SuppressWarnings("unchecked")
        final T[] result = (T[])Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        if (index > 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        result[index] = value;
        System.arraycopy(array, index, result, index + 1, array.length - index);
        return result;
    }

    public static <T> int indexOf(T[] array, T value) {
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(array[i], value)) {
                return i;
            }
        }
        return -1;
    }
}
