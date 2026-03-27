package dev.jwall64.databridge.mixin;

import java.lang.classfile.Opcode;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import dev.jwall64.databridge.impl.TimelineEvent;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sun.org.apache.xpath.internal.compiler.OpCodes;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.timeline.AttributeTrack;
import net.minecraft.world.timeline.Timeline;

@Mixin(Timeline.class)
public class MixinTimeline {

    @Unique
    private List<TimelineEvent> databridge$events;

    @WrapOperation(method="<clinit>", at=@At(value="FIELD", target="Lnet/minecraft/world/timeline/Timeline;DIRECT_CODEC", opcode = Opcodes.PUTSTATIC))
    private Codec<Timeline> wrapCodec(Operation<Codec<Timeline>> op) {
        
        Codec<Timeline> orig = op.call();
        Codec<Pair<Timeline, List<TimelineEvent>>> extra = Codec.pair(orig, TimelineEvent.CODEC.listOf());
        Codec<Timeline> out = extra.xmap(
            pair -> {
                ((MixinTimeline) (Object) pair.getFirst()).databridge$events = pair.getSecond();
                return pair.getFirst();
            }, 
            timeline -> {
                return Pair.of(timeline, ((MixinTimeline) (Object) timeline).databridge$events);
            });

        return out;
    }

}
