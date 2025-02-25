package dev.jwall64.databridge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TestFunction implements CommandFunction<CommandSourceStack> {

    private static final Logger log = LoggerFactory.getLogger(TestFunction.class);

    public static void funcTest(CommandSourceStack css,
                                CompoundTag tag,
                                ResourceLocation id,
                                CommandDispatcher<CommandSourceStack> dispatcher,
                                ExecutionContext<CommandSourceStack> ctx,
                                Frame frame,
                                TestState data) throws CommandSyntaxException {

        data.value++;
        css.getPlayerOrException().sendSystemMessage(Component.literal("Hello from a java method! (" + data.value + ")"));
    }

    public static void funcTestLoad(CommandSourceStack css,
                                CompoundTag tag,
                                ResourceLocation id,
                                CommandDispatcher<CommandSourceStack> dispatcher,
                                ExecutionContext<CommandSourceStack> ctx,
                                Frame frame,
                                TestState data) throws CommandSyntaxException {

        log.info("minecraft:load tag executed");
    }

    public static void funcInteract(CommandSourceStack css,
                                    CompoundTag tag,
                                    ResourceLocation id,
                                    CommandDispatcher<CommandSourceStack> dispatcher,
                                    ExecutionContext<CommandSourceStack> ctx,
                                    Frame frame,
                                    TestState data) throws CommandSyntaxException {

        EntitySelector selector = new EntitySelectorParser(new StringReader("@t"), true).parse();
        Entity entity = selector.findSingleEntity(css);

        ServerPlayer player = css.getPlayerOrException();

        data.value++;
        player.sendSystemMessage(player.getName()
                .copy()
                .append(Component.literal(" interacted with "))
                .append(entity.getName())
                .append(" (" + data.value + ")"));
    }

    private final ResourceLocation id;
    private final TestState data;

    public TestFunction(ResourceLocation id, TestState data) {
        this.id = id;
        this.data = data;
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
                        data.value++;
                        stack.getPlayerOrException().sendSystemMessage(Component.literal("Hello from a java object! (" + data.value + ")"));
                    } catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        };
    }
}
