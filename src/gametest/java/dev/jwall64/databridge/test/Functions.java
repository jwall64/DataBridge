package dev.jwall64.databridge.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.databridge.api.ServerFunctionUtil;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class Functions {

    private static final Logger LOGGER = LoggerFactory.getLogger(Functions.class);

    public static void increment(CommandSourceStack css,
                                 CompoundTag tag,
                                 Identifier id,
                                 CommandDispatcher<CommandSourceStack> dispatcher,
                                 ExecutionContext<CommandSourceStack> ctx,
                                 Frame frame,
                                 TestState data) throws CommandSyntaxException {

        LOGGER.info("databridge:increment called");

        if(tag == null) {
            data.value++;
        } else {
            data.value += tag.getIntOr("amount", 1);
        }
        css.sendSuccess(() -> Component.literal("Hello from a java method! (" + data.value + ")"), true);
        frame.returnSuccess(data.value);
    }


    public static void trigger(CommandSourceStack css,
                               CompoundTag tag,
                               Identifier id,
                               CommandDispatcher<CommandSourceStack> dispatcher,
                               ExecutionContext<CommandSourceStack> ctx,
                               Frame frame,
                               TestState data) throws CommandSyntaxException {

        if(tag == null) {
            LOGGER.info("databridge:trigger called");
        } else {
            LOGGER.info("databridge:trigger called with {}", tag);
            data.value = tag.getIntOr("value", 1);
        }

        data.lastEntity = css.getEntity();
        data.lastTrigger = ServerFunctionUtil.getTriggerEntity(css);

    }

}
