package dev.jwall64.databridge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestFunction implements CommandFunction<CommandSourceStack> {

    public static void funcTest(CommandSourceStack css,
                                CompoundTag tag,
                                ResourceLocation id,
                                CommandDispatcher<CommandSourceStack> dispatcher,
                                ExecutionContext<CommandSourceStack> ctx,
                                Frame frame) throws CommandSyntaxException {

        css.getPlayerOrException().sendSystemMessage(Component.literal("Hello from a java method!"));
    }

    private final ResourceLocation id;

    public TestFunction(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public @NotNull ResourceLocation id() {
        return id;
    }

    @Override
    public @NotNull InstantiatedFunction<CommandSourceStack> instantiate(@Nullable CompoundTag tag, CommandDispatcher<CommandSourceStack> dispatcher) throws FunctionInstantiationException {
        return new InstantiatedFunction<>() {
            @Override
            public @NotNull ResourceLocation id() {
                return id;
            }

            @Override
            public @NotNull List<UnboundEntryAction<CommandSourceStack>> entries() {
                return List.of((stack, ctx, frame) -> {
                    try {
                        stack.getPlayerOrException().sendSystemMessage(Component.literal("Hello from a java object!"));
                    } catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        };
    }
}
