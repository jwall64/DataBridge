package dev.jwall64.databridge.mixin;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.jwall64.databridge.impl.CustomDataExtension;
import dev.jwall64.databridge.impl.FunctionalComponent;

import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.component.CustomData;

@Mixin(CustomData.class)
@Implements(@Interface(iface=CustomDataExtension.class, prefix="databridge$"))
public class MixinCustomData {

    FunctionalComponent databridge$functionalComponent = null;

    @Inject(method="<init>", at=@At("TAIL"))
    private void onInit(CompoundTag tag, CallbackInfo ci) {
        databridge$functionalComponent = 
            tag.getCompound("databridge:functional")
                .flatMap(cmp -> FunctionalComponent.CODEC.decode(NbtOps.INSTANCE, cmp).result())
                .map(Pair::getFirst)
                .orElse(null);
    }

    public FunctionalComponent databridge$functionalComponent() {
        return databridge$functionalComponent;
    }

}
