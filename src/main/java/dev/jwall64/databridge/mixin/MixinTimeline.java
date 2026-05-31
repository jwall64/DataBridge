package dev.jwall64.databridge.mixin;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import dev.jwall64.databridge.impl.TimelineEvent;
import dev.jwall64.databridge.impl.TimelineExtension;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.world.timeline.Timeline;

@Mixin(Timeline.class)
@Implements(@Interface(iface = TimelineExtension.class, prefix = "databridge$"))
public class MixinTimeline {

    @Shadow @Final @Mutable
    public static Codec<Timeline> DIRECT_CODEC;

    @Unique
    private static Codec<Timeline> databridge$originalCodec;

    @Unique
    private List<TimelineEvent> databridge$events;

    @WrapOperation(method="<clinit>", at=@At(value="FIELD", target="Lnet/minecraft/world/timeline/Timeline;DIRECT_CODEC", opcode = Opcodes.PUTSTATIC))
    private static void wrapCodec(Codec<Timeline> orig, Operation<Void> op) {
        
        databridge$originalCodec = orig;

        Codec<Pair<Timeline, List<TimelineEvent>>> extra = Codec.pair(orig, TimelineEvent.CODEC.listOf().optionalFieldOf("events", List.of()).codec());
        Codec<Timeline> out = extra.xmap(
            pair -> {
                ((MixinTimeline) (Object) pair.getFirst()).databridge$events = pair.getSecond();
                return pair.getFirst();
            }, 
            timeline -> {
                return Pair.of(timeline, ((MixinTimeline) (Object) timeline).databridge$events);
            });

        op.call(out);
    }

    // The network codec is generated using the direct codec, but attempting to send events to the client prevents the packet from being serialized, so we need to return the original codec when the network codec is being generated. 
    // As of 26.1, the network codec initialization is the only time the direct codec is accessed during static initialization, so this should be safe. If more accesses are added in the future, this may need to be reworked.
    @WrapOperation(method="<clinit>", at=@At(value="FIELD", target="Lnet/minecraft/world/timeline/Timeline;DIRECT_CODEC", opcode = Opcodes.GETSTATIC))
    private static Codec<Timeline> wrapGetCodec(Operation<Timeline> op) {
        return databridge$originalCodec;
    }

    public List<TimelineEvent> databridge$getEvents() {
        return databridge$events;
    }

}
