package dev.jwall64.databridge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class StateObject<T> {

    private final Class<T> type;
    private final String factoryRef;
    private final MethodHandle factory;
    private final String destructorRef;
    private final MethodHandle destructor;

    private T value;

    private StateObject(Class<T> type, String factoryRef, MethodHandle factory, @Nullable String destructorRef, @Nullable MethodHandle destructor) {
        this.type = type;
        this.factoryRef = factoryRef;
        this.factory = factory;
        this.destructorRef = destructorRef;
        this.destructor = destructor;
    }

    private static <T> StateObject<T> lookup(Class<T> type, String factoryRef, String destructorRef) {
        try {

            MethodHandle destructor = null;
            if (destructorRef != null) {
                destructor = Utils.findMethod(destructorRef, destructorType(type));
            }

            return new StateObject<>(type, factoryRef, Utils.findMethod(factoryRef, type(type)), destructorRef, destructor);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static <T> MethodType type(Class<T> type) {
        return MethodType.methodType(type, ReloadableServerResources.class, ReloadableServerRegistries.LoadResult.class, ResourceManager.class, type);
    }

    private static <T> MethodType destructorType(Class<T> type) {
        return MethodType.methodType(Void.class, type);
    }

    public static final StateObject<Void> EMPTY = new StateObject<>(Void.class, "", MethodHandles.empty(type(Void.class)), null, null);

    public Class<T> type() {
        return type;
    }

    public String factoryRef() {
        return factoryRef;
    }

    public String destructorRef() {
        return destructorRef;
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
            T oldValue = value;
            value = type.cast(factory.invoke(resources, loadResult, resourceManager, value));
            if(destructor != null) {
                destructor.invoke(oldValue);
            }
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    public void unload() {
        if(destructor == null || value == null) return;
        try {
            destructor.invoke(value);
            value = null;
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
            Codec.STRING.fieldOf("factory").forGetter(StateObject::factoryRef),
            Codec.STRING.optionalFieldOf("destructor", null).forGetter(StateObject::destructorRef)
    ).apply(instance, StateObject::lookup));

}
