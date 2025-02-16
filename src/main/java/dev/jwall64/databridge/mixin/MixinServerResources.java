package dev.jwall64.databridge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.jwall64.databridge.CommandManager;
import dev.jwall64.databridge.ServerResourcesExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ReloadableServerResources.class)
@Implements(@Interface(iface=ServerResourcesExtension.class, prefix="databridge$"))
public class MixinServerResources {

    @Unique
    private CommandManager databridge$manager;

    public CommandManager databridge$getCommandManager() {
        return databridge$manager;
    }

    @Inject(method="<init>", at=@At("TAIL"))
    private void onInit(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, HolderLookup.Provider provider, FeatureFlagSet featureFlagSet, Commands.CommandSelection commandSelection, List<Registry.PendingTags<?>> list, int i, CallbackInfo ci) {
        databridge$manager = new CommandManager();
    }

    @Redirect(method="listeners", at=@At(value="INVOKE", target="Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;"))
    private List<Object> addListener(Object e1, Object e2, Object e3) {
        return List.of(e1, e2, e3, databridge$manager);
    }

    @WrapOperation(method="method_58296", at=@At(value = "INVOKE", target="Lnet/minecraft/server/packs/resources/ReloadInstance;done()Ljava/util/concurrent/CompletableFuture;"))
    private static CompletableFuture<?> onReload(ReloadInstance instance, Operation<CompletableFuture<?>> original, @Local ReloadableServerResources out) {

        CompletableFuture<?> og = original.call(instance);
        return og.thenApply(obj -> {
            ((ServerResourcesExtension) out).getCommandManager().registerAndForget(out.getCommands().getDispatcher());
            return obj;
        });
    }

}
