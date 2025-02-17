package dev.jwall64.databridge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.VanillaRegistries;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import dev.jwall64.databridge.DataBridgeRegistries;

@Mixin(VanillaRegistries.class)
public class MixinVanillaRegistries {

    @Shadow
    @Final
    @Mutable
    private static RegistrySetBuilder BUILDER;

    @WrapOperation(method="<clinit>", at=@At(value="FIELD", opcode= Opcodes.PUTSTATIC, target="Lnet/minecraft/data/registries/VanillaRegistries;BUILDER:Lnet/minecraft/core/RegistrySetBuilder;"))
    private static void addRegistries(RegistrySetBuilder value, Operation<Void> original) {
        value.add(DataBridgeRegistries.STATE_OBJECT, ctx -> {});
        value.add(DataBridgeRegistries.COMMAND, ctx -> {});
        value.add(DataBridgeRegistries.FUNCTION, ctx -> {});
        original.call(value);
    }

}
