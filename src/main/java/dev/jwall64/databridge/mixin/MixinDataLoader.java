package dev.jwall64.databridge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.RegistryDataLoader;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import dev.jwall64.databridge.CommandDefinition;
import dev.jwall64.databridge.DataBridgeRegistries;
import dev.jwall64.databridge.JavaFunctionDefinition;
import dev.jwall64.databridge.StateObject;

import java.util.List;
import java.util.stream.Stream;

@Mixin(RegistryDataLoader.class)
public class MixinDataLoader {

    @Shadow @Final @Mutable
    public static List<RegistryDataLoader.RegistryData<?>> WORLDGEN_REGISTRIES;

    @WrapOperation(method="<clinit>", at=@At(value="FIELD", opcode=Opcodes.PUTSTATIC, target="Lnet/minecraft/resources/RegistryDataLoader;WORLDGEN_REGISTRIES:Ljava/util/List;"))
    private static void redirectWorldgenRegistries(List<RegistryDataLoader.RegistryData<?>> value, Operation<Void> original) {
        original.call(Stream.concat(value.stream(), Stream.of(
                new RegistryDataLoader.RegistryData<>(DataBridgeRegistries.STATE_OBJECT, StateObject.CODEC, false),
                new RegistryDataLoader.RegistryData<>(DataBridgeRegistries.COMMAND, CommandDefinition.CODEC, false),
                new RegistryDataLoader.RegistryData<>(DataBridgeRegistries.FUNCTION, JavaFunctionDefinition.CODEC, false)
        )).toList());
    }

}
