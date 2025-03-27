package dev.jwall64.databridge;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import dev.jwall64.databridge.api.ServerStateObjects;

import java.util.Optional;
import java.util.function.Supplier;

public class TestCommand {

    public static int execute(CommandContext<CommandSourceStack> ctx, TestState data) {
        data.value++;
        ctx.getSource().sendSuccess(() -> Component.literal("Hello from a java method command! (" + data.value + ")"), false);
        return 1;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> builder(String id, LiteralArgumentBuilder<CommandSourceStack> builder, CommandBuildContext buildCtx, Supplier<TestState> data) {
        return builder.then(Commands.literal("test")
                .executes(ctx -> {
                    TestState state = data.get();
                    state.value++;
                    ctx.getSource().sendSuccess(() -> Component.literal("Hello from a java builder command! (" + state.value + ")"), false);
                    return 1;
                })
        ).then(Commands.literal("data")
                .executes(ctx -> {
                    Optional<TestState> state = ServerStateObjects.getStateObject(ctx.getSource().getServer(), TestState.class, ResourceLocation.tryBuild("databridge", "test"));
                    if(state.isEmpty() || state.get() != data.get()) return 0;

                    return 1;
                })
        );

    }

}
