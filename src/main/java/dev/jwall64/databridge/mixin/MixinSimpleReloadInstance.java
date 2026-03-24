package dev.jwall64.databridge.mixin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;

@Mixin(SimpleReloadInstance.class)
public class MixinSimpleReloadInstance {

    @Inject(method="create", at=@At("HEAD"))
    public void onCreate(ResourceManager resourceManager, List<PreparableReloadListener> listeners, Executor backgroundExecutor, Executor mainExecutor, CompletableFuture<Unit> initialTask, boolean enableProfiling) {



    }

}
