package dev.jwall64.databridge;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class TestCommand {

    public static int execute(CommandContext<CommandSourceStack> ctx, TestState data) {
        data.value++;
        ctx.getSource().sendSuccess(() -> Component.literal("Hello from a java method command! (" + data.value + ")"), false);
        return 1;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> builder(String id, LiteralArgumentBuilder<CommandSourceStack> builder, TestState data) {
        return builder.then(Commands.literal("test")
                .executes(ctx -> {
                    data.value++;
                    ctx.getSource().sendSuccess(() -> Component.literal("Hello from a java builder command! (" + data.value + ")"), false);
                    return 1;
                })
        );

    }

}
