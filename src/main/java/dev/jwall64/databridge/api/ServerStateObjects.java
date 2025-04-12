package dev.jwall64.databridge.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import dev.jwall64.databridge.impl.StateObject;

import java.util.Optional;

public interface ServerStateObjects {

    static <T> Optional<T> getStateObject(MinecraftServer server, Class<T> type, ResourceLocation id) {
        return StateObject.get(server.registryAccess(), type, id)
                .filter(StateObject::isPresent)
                .map(StateObject::value);
    }

    static<T> Optional<T> getStateObject(RegistryAccess access, Class<T> type, ResourceLocation id) {
        return StateObject.get(access, type, id)
                .filter(StateObject::isPresent)
                .map(StateObject::value);
    }

}
