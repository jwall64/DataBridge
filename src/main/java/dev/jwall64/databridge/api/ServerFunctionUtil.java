package dev.jwall64.databridge.api;

import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import dev.jwall64.databridge.impl.Utils;

import java.util.function.Function;

public interface ServerFunctionUtil {

    static void executeFunction(MinecraftServer server, ResourceLocation id, @Nullable CompoundTag with, @Nullable Function<CommandSourceStack, CommandSourceStack> sourceTransformer, CommandResultCallback callback) {
        Utils.executeFunction(server, id, with, sourceTransformer, callback);
    }

    static void executeFunctionTag(MinecraftServer server, ResourceLocation tag, @Nullable CompoundTag with, @Nullable Function<CommandSourceStack, CommandSourceStack> sourceTransformer) {
        Utils.executeFunctionTag(server, tag, with, sourceTransformer);
    }

}
