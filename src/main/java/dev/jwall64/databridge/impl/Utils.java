package dev.jwall64.databridge.impl;

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
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.permissions.PermissionLevel;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.serialization.Codec;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

@ApiStatus.Internal
public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static MethodHandle findMethod(String ref, Class<?> returnType, Class<?>... params)
            throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException {

        MethodType type = MethodType.methodType(returnType, params);
        return findMethod(ref, type);

    }

    public static MethodHandle findMethod(String ref, MethodType type)
            throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException {

        String[] parts = ref.split("::");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Expected a function value in the form <fully.qualified.class.Name>::<method>");
        }

        String className = parts[0];
        String functionName = parts[1];

        Class<?> clazz = Class.forName(className);
        return MethodHandles.lookup().findStatic(clazz, functionName, type);

    }

    public static void executeFunction(ServerFunctionManager manager, CommandFunction<CommandSourceStack> function,
            CompoundTag with, Function<CommandSourceStack, CommandSourceStack> sourceTransformer,
            CommandResultCallback resultCallback) {
        try {
            InstantiatedFunction<CommandSourceStack> instance = function.instantiate(with, manager.getDispatcher());
            CommandSourceStack source = sourceTransformer == null
                    ? manager.getGameLoopSender()
                    : sourceTransformer.apply(manager.getGameLoopSender());
            Commands.executeCommandInContext(source, ctx -> {
                ExecutionContext.queueInitialFunctionCall(ctx, instance, source, resultCallback);
            });
        } catch (FunctionInstantiationException ex) {
            log.error("An error occurred while instantiating a function!", ex);
        }

    }

    public static void executeFunction(MinecraftServer server, Identifier id, CompoundTag with,
            Function<CommandSourceStack, CommandSourceStack> sourceTransformer, CommandResultCallback resultCallback) {
        ServerFunctionManager manager = server.getFunctions();
        manager.get(id).ifPresent(func -> executeFunction(manager, func, with, sourceTransformer, resultCallback));
    }

    public static void executeFunctionTag(MinecraftServer server, Identifier id, CompoundTag with,
            Function<CommandSourceStack, CommandSourceStack> sourceTransformer) {
        ServerFunctionManager manager = server.getFunctions();
        server.getFunctions().getTag(id)
                .forEach(func -> executeFunction(manager, func, with, sourceTransformer, CommandResultCallback.EMPTY));
    }

    public static final Codec<PermissionLevel> PERMISSION_LEVEL_OR_INT = Codec.withAlternative(PermissionLevel.CODEC, PermissionLevel.INT_CODEC);

}
