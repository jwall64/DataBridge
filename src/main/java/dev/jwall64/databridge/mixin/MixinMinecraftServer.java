package dev.jwall64.databridge.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.jwall64.databridge.impl.DataBridgeRegistries;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @Shadow public abstract RegistryAccess.Frozen registryAccess();

    @Inject(method="loadLevel", at=@At(value="HEAD"))
    private void onInit(CallbackInfo ci) {
        registryAccess().lookup(DataBridgeRegistries.STATE_OBJECT).ifPresent(state -> {
            state.forEach(obj -> {
                obj.init((MinecraftServer) (Object) this);
            });
        });
    }

    @Inject(method="stopServer", at=@At("HEAD"))
    private void onStop(CallbackInfo ci) {
        registryAccess().lookup(DataBridgeRegistries.STATE_OBJECT).ifPresent(state -> {
            state.forEach(obj -> {
                obj.unload((MinecraftServer) (Object) this);
            });
        });
    }

    @Inject(method="method_29440", at=@At("TAIL"))
    private void onReload(CallbackInfo ci) {

        registryAccess().lookup(DataBridgeRegistries.STATE_OBJECT).ifPresent(state -> {
            state.forEach(obj -> {
                obj.reload((MinecraftServer) (Object) this);
            });
        });

    }

}
