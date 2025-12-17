package dev.jwall64.databridge.impl;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class DataBridgeRegistries {

    public static final ResourceKey<Registry<StateObject<?>>> STATE_OBJECT = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("state_object"));
    public static final ResourceKey<Registry<CommandDefinition>> COMMAND = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("command"));
    public static final ResourceKey<Registry<JavaFunctionDefinition>> FUNCTION = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("function"));

}
