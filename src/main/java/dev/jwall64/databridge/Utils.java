package dev.jwall64.databridge;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class Utils {

    public static MethodHandle findMethod(String ref, Class<?> returnType, Class<?>... params) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException {

        MethodType type = MethodType.methodType(returnType, params);
        return findMethod(ref, type);

    }

    public static MethodHandle findMethod(String ref, MethodType type) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException {

        String[] parts = ref.split("::");
        if(parts.length != 2) {
            throw new IllegalArgumentException("Expected a function value in the form <fully.qualified.class.Name>::<method>");
        }

        String className = parts[0];
        String functionName = parts[1];

        Class<?> clazz = Class.forName(className);
        return MethodHandles.lookup().findStatic(clazz, functionName, type);

    }

}
