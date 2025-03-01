package dev.jwall64.databridge;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class DataBridgeRegistries {

    public static final String NAMESPACE = "databridge";

    public static final ResourceKey<Registry<StateObject<?>>> STATE_OBJECT = ResourceKey.createRegistryKey(ResourceLocation.tryBuild(NAMESPACE, "state_object"));
    public static final ResourceKey<Registry<CommandDefinition>> COMMAND = ResourceKey.createRegistryKey(ResourceLocation.tryBuild(NAMESPACE, "command"));
    public static final ResourceKey<Registry<JavaFunctionDefinition>> FUNCTION = ResourceKey.createRegistryKey(ResourceLocation.tryBuild(NAMESPACE, "function"));

}
