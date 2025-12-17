package dev.jwall64.databridge.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import dev.jwall64.databridge.impl.StateObject;

import java.util.Optional;

public interface ServerStateObjects {

    static <T> Optional<T> getStateObject(MinecraftServer server, Class<T> state, String id) {
        return getStateObject(server, state, Identifier.tryParse(id));
    }

    static <T> Optional<T> getStateObject(MinecraftServer server, Class<T> type, Identifier id) {
        return StateObject.get(server.registryAccess(), type, id)
                .filter(StateObject::isPresent)
                .map(StateObject::value);
    }

    static <T> Optional<T> getStateObject(RegistryAccess access, Class<T> state, String id) {
        return getStateObject(access, state, Identifier.tryParse(id));
    }

    static <T> Optional<T> getStateObject(RegistryAccess access, Class<T> type, Identifier id) {
        return StateObject.get(access, type, id)
                .filter(StateObject::isPresent)
                .map(StateObject::value);
    }

}
