package dev.jwall64.databridge.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;
import java.util.function.Supplier;

@ApiStatus.Internal
public class StateObject<T> implements Supplier<T> {

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

    private static <T> StateObject<T> lookup(Class<T> type, String factoryRef, Optional<String> destructorRef) {
        try {

            MethodHandle destructor = null;
            if (destructorRef.isPresent()) {
                destructor = Utils.findMethod(destructorRef.get(), destructorType(type));
            }

            return new StateObject<>(type, factoryRef, Utils.findMethod(factoryRef, type(type)), destructorRef.orElse(null), destructor);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static <T> MethodType type(Class<T> type) {
        return MethodType.methodType(type, MinecraftServer.class, type);
    }

    private static <T> MethodType destructorType(Class<T> type) {
        return MethodType.methodType(Void.TYPE, MinecraftServer.class, type);
    }

    public static final StateObject<Void> EMPTY = new StateObject<>(Void.class, "", MethodHandles.empty(type(Void.class)), null, null);

    public Class<T> type() {
        return type;
    }

    public String factoryRef() {
        return factoryRef;
    }

    public Optional<String> destructorRef() {
        return Optional.ofNullable(destructorRef);
    }

    public MethodHandle factory() {
        return factory;
    }

    public T value() {
        assert value != null;
        return value;
    }

    public void reload(MinecraftServer server) {
        if(this == EMPTY) return;
        try {
            T oldValue = value;
            value = type.cast(factory.invoke(server, oldValue));
            if(destructor != null && oldValue != null) {
                destructor.invoke(server, oldValue);
            }
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    public boolean isPresent() {
        return value != null;
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<StateObject<T>> get(RegistryAccess access, Class<T> type, Identifier location) {

        return access.lookup(DataBridgeRegistries.STATE_OBJECT)
                .map(reg -> reg.getValue(location))
                .filter(obj -> type.isAssignableFrom(obj.type))
                .map(obj -> (StateObject<T>) obj);
    }

    public void init(MinecraftServer server) {
        if(this == EMPTY) return;
        try {
            value = type.cast(factory.invoke(server, null));
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    public void unload(MinecraftServer server) {
        if(destructor == null || value == null) return;
        try {
            destructor.invoke(server, value);
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
            Codec.STRING.optionalFieldOf("destructor").forGetter(StateObject::destructorRef)
    ).apply(instance, StateObject::lookup));

    @Override
    @Nullable
    public T get() {
        return value;
    }
}
