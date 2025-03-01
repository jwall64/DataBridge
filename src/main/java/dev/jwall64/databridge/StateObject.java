package dev.jwall64.databridge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class StateObject<T> {

    private final Class<T> type;
    private final String factoryRef;
    private final MethodHandle factory;

    private T value;

    private StateObject(Class<T> type, String factoryRef, MethodHandle factory) {
        this.type = type;
        this.factoryRef = factoryRef;
        this.factory = factory;
    }

    private static <T> StateObject<T> lookup(Class<T> type, String factoryRef) {
        try {
            return new StateObject<>(type, factoryRef, Utils.findMethod(factoryRef, type(type)));
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static <T> MethodType type(Class<T> type) {
        return MethodType.methodType(type, ReloadableServerResources.class, ReloadableServerRegistries.LoadResult.class, ResourceManager.class, type);
    }

    public static final StateObject<Void> EMPTY = new StateObject<>(Void.class, "", MethodHandles.empty(type(Void.class)));

    public Class<T> type() {
        return type;
    }

    public String factoryRef() {
        return factoryRef;
    }

    public MethodHandle factory() {
        return factory;
    }

    public T value() {
        assert value != null;
        return value;
    }

    public void reload(ReloadableServerResources resources, ReloadableServerRegistries.LoadResult loadResult, ResourceManager resourceManager) {
        if(this == EMPTY) return;
        try {
            value = type.cast(factory.invoke(resources, loadResult, resourceManager, value));
        } catch (Throwable th) {
            throw new RuntimeException(th);
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
            CLASS_CODEC.fieldOf("type").forGetter(StateObject::type),
            Codec.STRING.fieldOf("factory").forGetter(StateObject::factoryRef)
    ).apply(instance, StateObject::lookup));

}
