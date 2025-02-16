package dev.jwall64.databridge;

import java.lang.reflect.Method;

public class Utils {

    public static Method findMethod(String ref, Class<?>... params) throws NoSuchMethodException, ClassNotFoundException {

        String[] parts = ref.split("::");
        if(parts.length != 2) {
            throw new IllegalArgumentException("Expected a function value in the form <fully.qualified.class.Name>::<method>");
        }

        String className = parts[0];
        String functionName = parts[1];

        Class<?> clazz = Class.forName(className);
        return clazz.getMethod(functionName, params);
    }

}
