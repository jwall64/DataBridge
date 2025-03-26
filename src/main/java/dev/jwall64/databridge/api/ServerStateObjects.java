package dev.jwall64.databridge.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import dev.jwall64.databridge.impl.StateObject;

import java.util.Optional;

public interface ServerStateObjects {

    static <T> Optional<T> getStateObject(MinecraftServer server, Class<T> type, ResourceLocation id) {
        return StateObject.get(server, type, id)
                .filter(StateObject::isPresent)
                .map(StateObject::value);
    }

}
