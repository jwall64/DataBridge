package dev.jwall64.databridge.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

public class TimelineEvent implements Comparable<TimelineEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimelineEvent.class);

    private final Optional<Long> tick;
    private final Identifier functionName;
    private final CompoundTag with;

    private boolean resolved = false;
    private CommandFunction<CommandSourceStack> function;

    private boolean instantiated = false;
    private InstantiatedFunction<CommandSourceStack> instance;

    public TimelineEvent(Optional<Long> tick, Identifier functionName, CompoundTag with) {
        this.tick = tick;
        this.functionName = functionName;
        this.with = with;
    }

    public Optional<Long> tick() { return tick; }
    public Identifier function() { return functionName; }
    public CompoundTag with() { return with; }

    public int compareTo(TimelineEvent other) {
        return Long.compare(this.tick.orElse(Long.MAX_VALUE), other.tick.orElse(Long.MAX_VALUE));
    }

    public void execute(MinecraftServer server, long tick) {
        
        if(this.tick.isPresent() && this.tick.get() != tick) {
            return;
        }

        if(!resolved) {
            function = server.getFunctions().get(functionName).orElse(null);
            resolved = true;
        }
        if(function == null) return;

        if(!instantiated) {
            with.putLong("tick", tick);
            try {
                instance = function.instantiate(with, server.getCommands().getDispatcher());
            } catch(FunctionInstantiationException ex) {
                LOGGER.error("Unable to instantiate function for timeline event!", ex);
                return;
            }
            instantiated = this.tick.isPresent();
        } 

        if(instance == null) return;

        CommandSourceStack stack = server.getFunctions().getGameLoopSender();
        Commands.executeCommandInContext(stack, ctx -> {
            ExecutionContext.queueInitialFunctionCall(ctx, instance, stack, CommandResultCallback.EMPTY);
        });

    }

    public static final Codec<TimelineEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.optionalFieldOf("tick").forGetter(TimelineEvent::tick),
        Identifier.CODEC.fieldOf("function").forGetter(TimelineEvent::function),
        CompoundTag.CODEC.fieldOf("with").forGetter(TimelineEvent::with)
    ).apply(instance, TimelineEvent::new));

}
