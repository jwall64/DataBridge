package dev.jwall64.databridge;

import net.minecraft.core.Registry;

public interface ServerFunctionLibraryExtension {

    void addJavaFunctions(Registry<JavaFunctionDefinition> registry);

}
