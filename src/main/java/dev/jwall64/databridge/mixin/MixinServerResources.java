package dev.jwall64.databridge.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.jwall64.databridge.impl.DataBridgeRegistries;
import dev.jwall64.databridge.impl.ServerFunctionLibraryExtension;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerResources.class)
public abstract class MixinServerResources {

    @Inject(method = "method_58296", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/SimpleReloadInstance;create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private static void injectFunctions(FeatureFlagSet featureFlagSet, Commands.CommandSelection commandSelection,
                                        List<Registry.PendingTags<?>> list, PermissionSet permissions, ResourceManager resourceManager,
                                        Executor executor, Executor executor2, ReloadableServerRegistries.LoadResult loadResult,
                                        CallbackInfoReturnable<CompletionStage<?>> cir, @Local ReloadableServerResources built) {

        RegistryAccess.Frozen access = loadResult.layers().compositeAccess();

        CommandBuildContext ctx = CommandBuildContext.simple(loadResult.lookupWithUpdatedTags(), featureFlagSet);
        access.lookupOrThrow(DataBridgeRegistries.COMMAND).entrySet().forEach(entry -> {

            Identifier id = entry.getKey().identifier();
            String name = built.getCommands().getDispatcher().getRoot().getChild(id.getPath()) == null ? id.getPath()
                    : id.toString();

            LiteralArgumentBuilder<CommandSourceStack> builder = entry.getValue().create(name, ctx,
                    commandSelection);
            if (builder != null) {
                built.getCommands().getDispatcher().register(builder);
            }
        });

        ((ServerFunctionLibraryExtension) built.getFunctionLibrary())
                .addJavaFunctions(access.lookupOrThrow(DataBridgeRegistries.FUNCTION));
    }

}
