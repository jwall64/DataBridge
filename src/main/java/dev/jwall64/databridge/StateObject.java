package dev.jwall64.databridge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public record StateObject<T>(Class<T> type, T value) {

    public static final StateObject<Void> EMPTY = new StateObject<>(Void.class, null);

    private static <T> StateObject<T> build(Class<T> type) {
        try {
            Constructor<T> con = type.getConstructor();
            return new StateObject<>(type, con.newInstance());
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static final Codec<Class<?>> CLASS_CODEC = Codec.STRING.xmap(str -> {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }, Class::getCanonicalName);

    public static final Codec<StateObject<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CLASS_CODEC.fieldOf("class").forGetter(StateObject::type)
    ).apply(instance, StateObject::build));

}
