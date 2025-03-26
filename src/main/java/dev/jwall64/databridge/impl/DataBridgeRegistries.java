package dev.jwall64.databridge.impl;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class DataBridgeRegistries {

    public static final ResourceKey<Registry<StateObject<?>>> STATE_OBJECT = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("state_object"));
    public static final ResourceKey<Registry<CommandDefinition>> COMMAND = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("command"));
    public static final ResourceKey<Registry<JavaFunctionDefinition>> FUNCTION = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("function"));

}
