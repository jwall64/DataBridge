package dev.jwall64.databridge.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import dev.jwall64.databridge.DataBridgeRegistries;

import java.util.concurrent.CompletableFuture;

@Mixin(ReloadableServerResources.class)
public abstract class MixinServerResources {

    @WrapOperation(method="method_58296", at=@At(value = "INVOKE", target="Lnet/minecraft/server/packs/resources/ReloadInstance;done()Ljava/util/concurrent/CompletableFuture;"))
    private static CompletableFuture<?> onReload(ReloadInstance instance, Operation<CompletableFuture<?>> original, @Local ReloadableServerResources built, @Local(argsOnly = true) ReloadableServerRegistries.LoadResult loadResult) {
        CompletableFuture<?> og = original.call(instance);
        return og.thenApply(obj -> {
            loadResult.layers().compositeAccess().lookupOrThrow(DataBridgeRegistries.COMMAND).entrySet().forEach(entry ->
                    built.getCommands().getDispatcher().register(entry.getValue().create()));

            ImmutableMap.Builder<ResourceLocation, CommandFunction<CommandSourceStack>> functions = ImmutableMap.builder();
            functions.putAll(built.getFunctionLibrary().getFunctions());
            loadResult.layers().compositeAccess().lookupOrThrow(DataBridgeRegistries.FUNCTION).entrySet().forEach(entry -> {
                functions.put(entry.getKey().location(), entry.getValue().getFunction(entry.getKey().location()));
            });
            ((AccessorServerFunctionLibrary) built.getFunctionLibrary()).setFunctions(functions.build());

            return obj;
        });
    }

}
