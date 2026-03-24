package dev.jwall64.databridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import dev.jwall64.databridge.impl.DataBridgeRegistries;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.ConcurrentHolderGetter;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(ResourceManagerRegistryLoadTask.class)
public abstract class MixinRegistryLoadTask<T> extends RegistryLoadTask<T> {

    public MixinRegistryLoadTask(RegistryDataLoader.RegistryData<T> data, Lifecycle lifecycle, Map<ResourceKey<?>, Exception> loadingErrors) {
        super(data, lifecycle, loadingErrors);
    }

    @Inject(method="lambda$load$3", at=@At(value="INVOKE", target="Lnet/minecraft/tags/TagLoader$ElementLookup;fromGetters(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/HolderGetter;Lnet/minecraft/core/HolderGetter;)Lnet/minecraft/tags/TagLoader$ElementLookup;"), cancellable = true)
    private void onLoadTags(Map<ResourceKey<T>, T> loadedEntries, CallbackInfo ci) {
        if(registryKey().equals(DataBridgeRegistries.FUNCTION)) {
            ci.cancel();
        }
    }



}
