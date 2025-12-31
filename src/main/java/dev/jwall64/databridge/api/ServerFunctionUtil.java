package dev.jwall64.databridge.api;

import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import dev.jwall64.databridge.impl.CommandSourceStackExtension;
import dev.jwall64.databridge.impl.Utils;

import java.util.function.Function;

public interface ServerFunctionUtil {

    /**
     * Executes the function with the given ID on the server.
     * @param server The server to run the function on
     * @param id the ID of the function to run
     * @param with the parameters of the function
     * @param sourceTransformer some logic to transform the command source before invoking the function
     * @param callback some code to run when the function returns
     */
    static void executeFunction(MinecraftServer server, Identifier id, @Nullable CompoundTag with, @Nullable Function<CommandSourceStack, CommandSourceStack> sourceTransformer, CommandResultCallback callback) {
        Utils.executeFunction(server, id, with, sourceTransformer, callback);
    }

    static void executeFunctionTag(MinecraftServer server, Identifier tag, @Nullable CompoundTag with, @Nullable Function<CommandSourceStack, CommandSourceStack> sourceTransformer) {
        Utils.executeFunctionTag(server, tag, with, sourceTransformer);
    }

    static Entity getTriggerEntity(CommandSourceStack commandSourceStack) {
        return ((CommandSourceStackExtension) commandSourceStack).getTriggerEntity();
    }

    static void setTriggerEntity(CommandSourceStack commandSourceStack, Entity entity) {
        ((CommandSourceStackExtension) commandSourceStack).setTriggerEntity(entity);
    }

}
