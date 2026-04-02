package dev.jwall64.databridge.impl;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
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

public class FunctionRef {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionRef.class);

    private final Identifier id;
    private final Optional<CompoundTag> with;

    private WeakReference<CommandFunction<CommandSourceStack>> function = new WeakReference<CommandFunction<CommandSourceStack>>(null);
    private InstantiatedFunction<CommandSourceStack> instance;

    public FunctionRef(Identifier id, Optional<CompoundTag> with) {
        this.id = id;
        this.with = with;
    }

    public Identifier id() { return id; }
    public Optional<CompoundTag> with() { return with; }

    public void execute(MinecraftServer server, @Nullable CompoundTag tag, Function<CommandSourceStack, CommandSourceStack> sourceTransformer) {

        CommandFunction<CommandSourceStack> func = function.get();

        if(func == null) {
            instance = null;
            func = server.getFunctions().get(id).orElse(null);
            if(func == null) return;
            function = new WeakReference<>(func);
        }

        InstantiatedFunction<CommandSourceStack> inst = instance;
        if(tag != null) {

            CompoundTag realWith = null;
            if(with.isEmpty()) {
                realWith = tag;
            } else {
                realWith = with.get().copy().merge(tag);
            }
        
            try {
                inst = func.instantiate(realWith, server.getCommands().getDispatcher());
            } catch(FunctionInstantiationException ex) {
                LOGGER.error("An error occurred while instantiating a function!", ex);
                return;
            }

        } else if(inst == null) {

            try {
                inst = func.instantiate(with.orElse(null), server.getCommands().getDispatcher());
            } catch(FunctionInstantiationException ex) {
                LOGGER.error("An error occurred while instantiating a function!", ex);
                return;
            }

            // Cache tag-less instance
            instance = inst;
        }

        CommandSourceStack css = sourceTransformer.apply(server.getFunctions().getGameLoopSender());

        final InstantiatedFunction<CommandSourceStack> finalInst = inst;
        Commands.executeCommandInContext(css, exeCtx -> {
            ExecutionContext.queueInitialFunctionCall(exeCtx, finalInst, css, CommandResultCallback.EMPTY);
        });

    }

    public static final Codec<FunctionRef> CODEC = Codec.withAlternative(
        RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(FunctionRef::id),
            CompoundTag.CODEC.optionalFieldOf("with").forGetter(FunctionRef::with)
        ).apply(instance, FunctionRef::new)), 
        Codec.STRING.xmap(str -> new FunctionRef(Identifier.tryParse(str), Optional.empty()), ref -> ref.id().toString())
    );

}
