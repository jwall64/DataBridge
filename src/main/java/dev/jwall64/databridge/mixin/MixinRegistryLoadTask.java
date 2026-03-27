package dev.jwall64.databridge.mixin;

import org.spongepowered.asm.mixin.Mixin;

import dev.jwall64.databridge.impl.DataBridgeRegistries;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.serialization.Lifecycle;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;

import java.util.Map;

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
