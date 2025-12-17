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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

public class TestFunction implements CommandFunction<CommandSourceStack> {

    private static final Logger log = LoggerFactory.getLogger(TestFunction.class);

    public static void funcTest(CommandSourceStack css,
                                CompoundTag tag,
                                Identifier id,
                                CommandDispatcher<CommandSourceStack> dispatcher,
                                ExecutionContext<CommandSourceStack> ctx,
                                Frame frame,
                                TestState data) throws CommandSyntaxException {

        data.value++;
        css.sendSuccess(() -> Component.literal("Hello from a java method! (" + data.value + ")"), true);
        frame.returnSuccess(data.value);
    }

    public static void funcTestLoad(CommandSourceStack css,
                                CompoundTag tag,
                                Identifier id,
                                CommandDispatcher<CommandSourceStack> dispatcher,
                                ExecutionContext<CommandSourceStack> ctx,
                                Frame frame,
                                TestState data) throws CommandSyntaxException {

        log.info("minecraft:load tag executed");
        frame.returnSuccess(data.value);
    }

    public static void funcInteract(CommandSourceStack css,
                                    CompoundTag tag,
                                    Identifier id,
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
        frame.returnSuccess(data.value);
    }

    private final Identifier id;
    private final Supplier<TestState> data;

    public TestFunction(Identifier id, Supplier<TestState> data) {
        this.id = id;
        this.data = data;
    }

    public static CommandFunction<CommandSourceStack> create(Identifier id, Supplier<TestState> data) {
        return new TestFunction(id, data);
    }


    @Override
    public @NotNull Identifier id() {
        return id;
    }

    @Override
    public @NotNull InstantiatedFunction<CommandSourceStack> instantiate(@Nullable CompoundTag tag, CommandDispatcher<CommandSourceStack> dispatcher) throws FunctionInstantiationException {
        return new InstantiatedFunction<>() {
            @Override
            public @NotNull Identifier id() {
                return id;
            }

            @Override
            public @NotNull List<UnboundEntryAction<CommandSourceStack>> entries() {
                return List.of((stack, ctx, frame) -> {
                    TestState state = data.get();
                    state.value++;
                    stack.sendSuccess(() -> (Component.literal("Hello from a java object! (" + state.value + ")")), false);
                    frame.returnSuccess(state.value);
                });
            }
        };
    }
}
