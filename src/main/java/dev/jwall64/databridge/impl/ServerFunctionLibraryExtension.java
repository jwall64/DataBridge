package dev.jwall64.databridge.impl;

import net.minecraft.core.Registry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface ServerFunctionLibraryExtension {

    void addJavaFunctions(Registry<JavaFunctionDefinition> registry);

}
