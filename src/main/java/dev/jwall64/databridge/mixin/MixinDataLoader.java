package dev.jwall64.databridge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryValidator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import dev.jwall64.databridge.impl.CommandDefinition;
import dev.jwall64.databridge.impl.CustomClickEventHandler;
import dev.jwall64.databridge.impl.DataBridgeRegistries;
import dev.jwall64.databridge.impl.JavaFunctionDefinition;
import dev.jwall64.databridge.impl.StateObject;

import java.util.List;
import java.util.stream.Stream;

@Mixin(RegistryDataLoader.class)
public class MixinDataLoader {

    @Shadow @Final @Mutable
    public static List<RegistryDataLoader.RegistryData<?>> WORLDGEN_REGISTRIES;

    @WrapOperation(method="<clinit>", at=@At(value="FIELD", opcode=Opcodes.PUTSTATIC, target="Lnet/minecraft/resources/RegistryDataLoader;WORLDGEN_REGISTRIES:Ljava/util/List;"))
    private static void redirectWorldgenRegistries(List<RegistryDataLoader.RegistryData<?>> value, Operation<Void> original) {
        original.call(Stream.concat(value.stream(), Stream.of(
                new RegistryDataLoader.RegistryData<>(DataBridgeRegistries.STATE_OBJECT, StateObject.CODEC, RegistryValidator.none()),
                new RegistryDataLoader.RegistryData<>(DataBridgeRegistries.COMMAND, CommandDefinition.CODEC, RegistryValidator.none()),
                new RegistryDataLoader.RegistryData<>(DataBridgeRegistries.FUNCTION, JavaFunctionDefinition.CODEC, RegistryValidator.none()),
                new RegistryDataLoader.RegistryData<>(DataBridgeRegistries.CLICK_EVENT_HANDLER, CustomClickEventHandler.CODEC, RegistryValidator.none())
        )).toList());
    }

}
