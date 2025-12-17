package dev.jwall64.databridge.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionLevel;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@ApiStatus.Internal
public record CommandDefinition(Type type, String value, PermissionLevel permissionLevel,
        Optional<String> permissionNode, Commands.CommandSelection environment,
        Optional<Holder<StateObject<?>>> state) {

    private static final CommandExceptionType EXCEPTION = new CommandExceptionType() {
    };

    private static final Codec<Commands.CommandSelection> COMMAND_SELECTION_CODEC = Codec.STRING
            .xmap(str -> switch (str) {
                case "all" -> Commands.CommandSelection.ALL;
                case "dedicated" -> Commands.CommandSelection.DEDICATED;
                case "integrated" -> Commands.CommandSelection.INTEGRATED;
                default -> throw new IllegalStateException("Unexpected value: " + str);
            }, sel -> switch (sel) {
                case ALL -> "all";
                case DEDICATED -> "dedicated";
                case INTEGRATED -> "integrated";
            });

    public static final Codec<CommandDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Type.CODEC.optionalFieldOf("type", Type.ALIAS).forGetter(CommandDefinition::type),
            Codec.STRING.fieldOf("value").forGetter(CommandDefinition::value),
            PermissionLevel.CODEC.optionalFieldOf("permission_level", PermissionLevel.ALL).forGetter(CommandDefinition::permissionLevel),
            Codec.STRING.optionalFieldOf("permission_node").forGetter(CommandDefinition::permissionNode),
            COMMAND_SELECTION_CODEC.optionalFieldOf("environment", Commands.CommandSelection.ALL)
                    .forGetter(CommandDefinition::environment),
            RegistryFixedCodec.create(DataBridgeRegistries.STATE_OBJECT).optionalFieldOf("state_object")
                    .forGetter(CommandDefinition::state))
            .apply(instance, CommandDefinition::new));

    private boolean inSelection(Commands.CommandSelection commandSelection) {
        if (environment == Commands.CommandSelection.ALL || commandSelection == Commands.CommandSelection.ALL)
            return true;
        return environment == commandSelection;
    }

    @Nullable
    public LiteralArgumentBuilder<CommandSourceStack> create(String name,
            CommandBuildContext buildCtx,
            Commands.CommandSelection commandSelection) {

        if (!inSelection(commandSelection)) {
            return null;
        }

        LiteralArgumentBuilder<CommandSourceStack> out = switch (type) {
            case ALIAS -> Commands.literal(name).executes(ctx -> {

                MinecraftServer server = ctx.getSource().getServer();
                CommandSourceStack source = ctx.getSource().withPermission(server.getFunctionCompilationPermissions());

                server.getCommands().performPrefixedCommand(source, value);
                return 1;
            });
            case METHOD -> {
                try {
                    StateObject<?> obj = state.isPresent() ? state.orElseThrow().value() : StateObject.EMPTY;
                    MethodHandle method = Utils.findMethod(value, int.class, CommandContext.class, obj.type());
                    yield Commands.literal(name).executes(ctx -> {
                        try {
                            return (int) method.invoke(ctx, obj.value());
                        } catch (Throwable e) {
                            throw new CommandSyntaxException(EXCEPTION,
                                    () -> "Unable to execute command " + name + "! " + e.getMessage());
                        }
                    });
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            }
            case BUILDER -> fromBuilder(name, buildCtx);
        };

        if (permissionLevel != PermissionLevel.ALL) {
            if (permissionNode.isPresent()) {
                out = out.requires(Permissions.require(permissionNode.orElseThrow(), permissionLevel.id()));
            } else {
                out = out.requires(Commands.hasPermission(new PermissionCheck.Require(new Permission.HasCommandLevel(permissionLevel))));
            }
        }

        return out;
    }

    @SuppressWarnings("unchecked")
    private LiteralArgumentBuilder<CommandSourceStack> fromBuilder(String name, CommandBuildContext buildContext) {
        try {

            StateObject<?> obj = state.isPresent() ? state.orElseThrow().value() : StateObject.EMPTY;

            LiteralArgumentBuilder<CommandSourceStack> out = Commands.literal(name); 
            MethodPrefab permLevel = MethodPrefab.fromList(LiteralArgumentBuilder.class, List.of(
                Pair.of(String.class, name),
                Pair.of(LiteralArgumentBuilder.class, out),
                Pair.of(CommandBuildContext.class, buildContext),
                Pair.of(PermissionLevel.class, permissionLevel),
                Pair.of(String.class, permissionNode.orElse(null)),
                Pair.of(Supplier.class, obj)));

            MethodPrefab permNumber = MethodPrefab.fromList(LiteralArgumentBuilder.class, List.of(
                Pair.of(String.class, name),
                Pair.of(LiteralArgumentBuilder.class, out),
                Pair.of(CommandBuildContext.class, buildContext),
                Pair.of(int.class, permissionLevel.id()),
                Pair.of(String.class, permissionNode.orElse(null)),
                Pair.of(Supplier.class, obj)));

            MethodPrefab simple = MethodPrefab.fromList(LiteralArgumentBuilder.class, List.of(
                Pair.of(String.class, name),
                Pair.of(LiteralArgumentBuilder.class, out),
                Pair.of(CommandBuildContext.class, buildContext),
                Pair.of(Supplier.class, obj)));

            MethodPrefab[] prefabs = { permLevel, permNumber, simple };

            Throwable findMethodException = null;
            for(MethodPrefab prefab : prefabs) {
                try {
                    MethodHandle handle = Utils.findMethod(value, prefab.type());
                    try {
                        return (LiteralArgumentBuilder<CommandSourceStack>) (handle.invokeWithArguments(prefab.arguments()));
                    } catch(Throwable th) {
                        throw new RuntimeException("An exception occurred while loading a command!", th);
                    }
                } catch(NoSuchMethodException | IllegalAccessException e) {
                    findMethodException = e;
                }
            }

            throw new RuntimeException("Unable to access method handle for " + value + "!", findMethodException);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to access method handle for " + value + "!", e);
        }
    }

    private record MethodPrefab(MethodType type, Object[] arguments) {
        static MethodPrefab fromList(Class<?> returnType, List<Pair<Class<?>, Object>> arguments) {
            
            Class<?> types[] = new Class<?>[arguments.size()];
            Object[] args = new Object[arguments.size()];
            int index = 0;
            for(Pair<Class<?>, Object> ent : arguments) {
                types[index] = ent.getFirst();
                args[index++] = ent.getSecond();
            }

            return new MethodPrefab(MethodType.methodType(returnType, types), args);
        }
    }


    enum Type {
        ALIAS("alias"),
        METHOD("method"),
        BUILDER("builder");

        private final String id;

        Type(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static Type byId(String id) {
            for (Type t : Type.values()) {
                if (t.id.equals(id)) {
                    return t;
                }
            }
            return null;
        }

        public static final Codec<Type> CODEC = Codec.STRING.xmap(Type::byId, Type::getId);
    }

}
