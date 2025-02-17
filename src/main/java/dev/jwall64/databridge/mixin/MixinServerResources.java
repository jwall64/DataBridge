package dev.jwall64.databridge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.jwall64.databridge.DataBridgeRegistries;
import dev.jwall64.databridge.ServerFunctionLibraryExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerResources.class)
public abstract class MixinServerResources {

    @Inject(method="method_58296", at=@At(value="INVOKE", target="Lnet/minecraft/server/packs/resources/SimpleReloadInstance;create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private static void injectFunctions(FeatureFlagSet featureFlagSet, Commands.CommandSelection commandSelection,
                                        List<Registry.PendingTags<?>> list, int i, ResourceManager resourceManager,
                                        Executor executor, Executor executor2, ReloadableServerRegistries.LoadResult loadResult,
                                        CallbackInfoReturnable<CompletionStage<?>> cir, @Local ReloadableServerResources built) {

        ((ServerFunctionLibraryExtension) built.getFunctionLibrary()).addJavaFunctions(loadResult.layers().compositeAccess().lookupOrThrow(DataBridgeRegistries.FUNCTION));
    }

    @WrapOperation(method="method_58296", at=@At(value = "INVOKE", target="Lnet/minecraft/server/packs/resources/ReloadInstance;done()Ljava/util/concurrent/CompletableFuture;"))
    private static CompletableFuture<?> onReload(ReloadInstance instance, Operation<CompletableFuture<?>> original, @Local ReloadableServerResources built, @Local(argsOnly = true) ReloadableServerRegistries.LoadResult loadResult) {
        CompletableFuture<?> og = original.call(instance);
        return og.thenApply(obj -> {
            loadResult.layers().compositeAccess().lookupOrThrow(DataBridgeRegistries.COMMAND).entrySet().forEach(entry ->
                    built.getCommands().getDispatcher().register(entry.getValue().create()));

            return obj;
        });
    }

}
