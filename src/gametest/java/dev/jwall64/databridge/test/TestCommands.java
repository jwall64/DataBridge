package dev.jwall64.databridge.test;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class TestCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCommands.class);

    public static int execute(CommandContext<CommandSourceStack> ctx, TestState state) {
        state.value++;
        LOGGER.info("Incremented value ({})", state.value);
        return 1;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> builder(String id, LiteralArgumentBuilder<CommandSourceStack> builder,
        CommandBuildContext buildCtx, Supplier<TestState> state) {

        return builder
            .executes(ctx -> {
                state.get().value++;
                LOGGER.info("Incremented value from builder ({})", state.get().value);
                return 1;
            })
            .then(Commands.argument("amount", IntegerArgumentType.integer())
                .executes(ctx -> {
                    int amount =  IntegerArgumentType.getInteger(ctx, "amount");
                    state.get().value += amount;
                    LOGGER.info("Incremented value by {} ({})", amount, state.get().value);
                    return 1;
                })
            );

    }


}
