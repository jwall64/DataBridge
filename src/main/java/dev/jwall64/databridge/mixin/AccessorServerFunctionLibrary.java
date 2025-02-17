package dev.jwall64.databridge.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionLibrary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ServerFunctionLibrary.class)
public interface AccessorServerFunctionLibrary {

    @Accessor("functions")
    @Mutable
    void setFunctions(Map<ResourceLocation, CommandFunction<CommandSourceStack>> functions);

}
