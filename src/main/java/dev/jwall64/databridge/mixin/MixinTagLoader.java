package dev.jwall64.databridge.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.jwall64.databridge.impl.DataBridgeRegistries;

import java.util.Optional;

@Mixin(TagLoader.class)
public class MixinTagLoader {

    @Inject(method="loadPendingTags", at=@At(value="INVOKE", target = "Lnet/minecraft/tags/TagLoader;<init>(Lnet/minecraft/tags/TagLoader$ElementLookup;Ljava/lang/String;)V"), cancellable = true)
    private static <T> void onLoadTags(ResourceManager resourceManager, Registry<T> registry, CallbackInfoReturnable<Optional<Registry.PendingTags<T>>> cir, @Local ResourceKey<T> key) {
        if(key == DataBridgeRegistries.FUNCTION) cir.setReturnValue(Optional.empty());
    }

}
