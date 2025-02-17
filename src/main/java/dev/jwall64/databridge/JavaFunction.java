package dev.jwall64.databridge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.util.List;

public class JavaFunction implements CommandFunction<CommandSourceStack> {

    private static final Logger log = LoggerFactory.getLogger(JavaFunction.class);
    private final ResourceLocation id;
    private final MethodHandle method;
    private final Object stateObject;

    public JavaFunction(ResourceLocation id, MethodHandle method, Object stateObject) {
        this.id = id;
        this.method = method;
        this.stateObject = stateObject;
    }

    @Override
    public @NotNull ResourceLocation id() {
        return id;
    }

    @Override
    public @NotNull InstantiatedFunction<CommandSourceStack> instantiate(@Nullable CompoundTag compoundTag, CommandDispatcher<CommandSourceStack> commandDispatcher) {
        return new Instance(compoundTag, commandDispatcher);
    }

    private class Instance implements InstantiatedFunction<CommandSourceStack>, UnboundEntryAction<CommandSourceStack> {

        private final List<UnboundEntryAction<CommandSourceStack>> actions;
        private final CompoundTag tag;
        private final CommandDispatcher<CommandSourceStack> dispatcher;

        public Instance(CompoundTag tag, CommandDispatcher<CommandSourceStack> dispatcher) {
            this.actions = List.of(this);
            this.tag = tag;
            this.dispatcher = dispatcher;
        }

        @Override
        public @NotNull ResourceLocation id() {
            return id;
        }

        @Override
        public @NotNull List<UnboundEntryAction<CommandSourceStack>> entries() {
            return actions;
        }

        @Override
        public void execute(CommandSourceStack stack, ExecutionContext<CommandSourceStack> ctx, Frame frame) {
            try {
                method.invoke(stack, tag, id, dispatcher, ctx, frame, stateObject);
            } catch (Throwable e) {
                log.error("An error occurred while executing a java method function!", e);
            }
        }

    }

}
