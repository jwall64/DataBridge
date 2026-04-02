package dev.jwall64.databridge.impl;

import java.lang.ref.WeakReference;
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
    private final CompoundTag with;

    private WeakReference<CommandFunction<CommandSourceStack>> function = new WeakReference<CommandFunction<CommandSourceStack>>(null);
    private InstantiatedFunction<CommandSourceStack> instance;

    public FunctionRef(Identifier id, CompoundTag with) {
        this.id = id;
        this.with = with;
    }

    public Identifier id() { return id; }
    public CompoundTag with() { return with; }

    public void execute(MinecraftServer server, @Nullable CompoundTag tag, Function<CommandSourceStack, CommandSourceStack> sourceTransformer) {

        CommandFunction<CommandSourceStack> func = function.get();

        if(func == null) {
            instance = null;
            func = server.getFunctions().get(id).orElse(null);
            if(func == null) return;
            function = new WeakReference<>(func);
        }

        if(instance == null) {
            tag = tag == null ? with : with.copy().merge(tag);
            try {
                instance = func.instantiate(tag, server.getCommands().getDispatcher());
            } catch(FunctionInstantiationException ex) {
                LOGGER.error("An error occurred while instantiating a function!", ex);
                return;
            }
        }

        CommandSourceStack css = sourceTransformer.apply(server.getFunctions().getGameLoopSender());
        Commands.executeCommandInContext(css, exeCtx -> {
            ExecutionContext.queueInitialFunctionCall(exeCtx, instance, css, CommandResultCallback.EMPTY);
        });

    }

    public static final Codec<FunctionRef> CODEC = Codec.withAlternative(
        RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(FunctionRef::id),
            CompoundTag.CODEC.fieldOf("with").forGetter(FunctionRef::with)
        ).apply(instance, FunctionRef::new)), 
        Codec.STRING.xmap(str -> new FunctionRef(Identifier.tryParse(str), new CompoundTag()), ref -> ref.id().toString())
    );

}
