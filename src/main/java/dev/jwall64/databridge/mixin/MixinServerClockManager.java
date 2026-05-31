package dev.jwall64.databridge.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.jwall64.databridge.impl.TimelineTracker;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.clock.ServerClockManager;

@Mixin(ServerClockManager.class)
public abstract class MixinServerClockManager {

    @Unique
    private List<TimelineTracker> databridge$trackers;

    @Inject(method="init", at=@At("TAIL"))
    public void onInit(MinecraftServer server, CallbackInfo ci) {

        databridge$trackers = new ArrayList<>();

        server.registryAccess().lookupOrThrow(Registries.TIMELINE).listElements().forEach(entry -> {
            TimelineTracker tracker = new TimelineTracker();
            tracker.init(server, entry.value());
            databridge$trackers.add(tracker);
        });        

    }
    
    @Inject(method="tick", at=@At(value="INVOKE", target="Lnet/minecraft/world/clock/ServerClockManager;setDirty()V"))
    public void onTick(CallbackInfo ci) {
        databridge$trackers.forEach(tracker -> tracker.tick());
    }

}
