package dev.jwall64.databridge.test;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import dev.jwall64.databridge.api.ServerFunctionUtil;
import dev.jwall64.databridge.api.ServerStateObjects;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;

public class GameTests implements CustomTestMethodInvoker {

    @Override
    public void invokeTestMethod(GameTestHelper helper, @NonNull Method method) throws ReflectiveOperationException {

        Optional<TestState> state = ServerStateObjects.getStateObject(helper.getLevel().registryAccess(), TestState.class, Identifier.tryParse("databridge:test"));
        if(state.isEmpty()) {
            helper.fail("State object was not present!");
            return;
        }

        TestState st = state.get();
        st.value = 0;
        method.invoke(this, helper, st);
    }


    //
    // Functions
    //
    @GameTest
    public void functionRuns(GameTestHelper helper, TestState state) {

        ServerFunctionUtil.executeFunction(helper.getLevel().getServer(), Identifier.tryParse("databridge:increment"), null, null, (success, result) -> {
            if(success) {
                if(state.value == 1) {
                    helper.succeed();
                } else {
                    helper.fail("Function did not increment the state object!");
                }
            } else {
                helper.fail("Function invocation failed!");
            }
        });
    }

    @GameTest
    public void vanillaFunctionRuns(GameTestHelper helper, TestState state) {

        ServerFunctionUtil.executeFunction(helper.getLevel().getServer(), Identifier.tryParse("databridge:ret"), null, null, (success, result) -> {
            if(success) {
                helper.succeed();
            } else {
                helper.fail("Function invocation failed!");
            }
        });
    }

    @GameTest
    public void functionRunsWithTag(GameTestHelper helper, TestState state) {

        CompoundTag tag = new CompoundTag();
        tag.putInt("amount", 30);
        ServerFunctionUtil.executeFunction(helper.getLevel().getServer(), Identifier.tryParse("databridge:increment"), tag, null, (success, result) -> {
            if(success) {
                if(state.value == 30) {
                    helper.succeed();
                } else {
                    helper.fail("Function did not increment the state object!");
                }
            } else {
                helper.fail("Function invocation failed!");
            }
        });
    }

    @GameTest
    public void vanillaFunctionRunsWithTag(GameTestHelper helper, TestState state) {

        CompoundTag tag = new CompoundTag();
        tag.putString("value", "30");
        ServerFunctionUtil.executeFunction(helper.getLevel().getServer(), Identifier.tryParse("databridge:ret_value"), tag, null, (success, result) -> {
            if(success) {
                if(result == 30) {
                    helper.succeed();
                } else {
                    helper.fail("Function returned invalid value");
                }
            } else {
                helper.fail("Function invocation failed!");
            }
        });
    }


    //
    // Commands
    //
    @GameTest
    public void methodCommandRuns(GameTestHelper helper, TestState state) {

        MinecraftServer server = helper.getLevel().getServer();
        CommandSourceStack css = server.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(css, "dbmethod");

        if(state.value == 1) {
            helper.succeed();
        } else {
            helper.fail("Command did not increment the state object!");
        }
    }

    @GameTest
    public void builderCommandRuns(GameTestHelper helper, TestState state) {

        MinecraftServer server = helper.getLevel().getServer();
        CommandSourceStack css = server.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(css, "dbbuilder");

        if(state.value == 1) {
            helper.succeed();
        } else {
            helper.fail("Command did not increment the state object!");
        }
    }

    @GameTest
    public void builderCommandRunsWithArgs(GameTestHelper helper, TestState state) {

        MinecraftServer server = helper.getLevel().getServer();
        CommandSourceStack css = server.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(css, "dbbuilder 30");

        if(state.value == 30) {
            helper.succeed();
        } else {
            helper.fail("Command did not increment the state object!");
        }
    }

    @GameTest
    public void commandsRunFromVanillaFunction(GameTestHelper helper, TestState state) {

        ServerFunctionUtil.executeFunction(helper.getLevel().getServer(), Identifier.tryParse("databridge:commands"), null, null, (success, result) -> {
            if(success) {
                if(state.value == 30) {
                    helper.succeed();
                } else {
                    helper.fail("Commands did not increment the state object properly! (" + state.value + " != 32)");
                }
            } else {
                helper.fail("Function invocation failed!");
            }
        });
    }

    //
    // Interaction Entities
    //
    private Pair<Interaction, Player> spawnInteraction(GameTestHelper helper, Map<String, CompoundTag> functions, Map<String, CompoundTag> attackFunctions) {

        ServerLevel level = helper.getLevel();
        Vec3 origin = helper.absoluteVec(Vec3.ZERO);

        Interaction entity = new Interaction(EntityTypes.INTERACTION, level);
        TagValueOutput vo = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        entity.saveWithoutId(vo);
        CompoundTag tag = vo.buildResult();
        
        if(functions != null) {
            CompoundTag functionTag = new CompoundTag();
            for(Map.Entry<String, CompoundTag> ent : functions.entrySet()) {
                functionTag.put(ent.getKey(), ent.getValue());
            }
            tag.put("functions", functionTag);
        }
        if(attackFunctions != null) {
            CompoundTag functionTag = new CompoundTag();
            for(Map.Entry<String, CompoundTag> ent : attackFunctions.entrySet()) {
                functionTag.put(ent.getKey(), ent.getValue());
            }
            tag.put("attack_functions", functionTag);
        }
        
        ValueInput vi = TagValueInput.create(ProblemReporter.DISCARDING, level.getServer().registryAccess(), tag);
        entity.load(vi);

        level.addFreshEntity(entity);
        entity.teleportTo(origin.x, origin.y, origin.z);

        DummyPlayer player = new DummyPlayer(level, new GameProfile(UUID.randomUUID(), "Player0"));
        level.addFreshEntity(player);
        player.teleportTo(origin.x, origin.y, origin.z);

        return Pair.of(entity, player);
    }

    @GameTest
    public void interactionRunsFunctions(GameTestHelper helper, TestState state) {
        
        Pair<Interaction, Player> entities = spawnInteraction(helper, Map.of("databridge:increment", new CompoundTag()), null);
        entities.getFirst().interact(entities.getSecond(), InteractionHand.MAIN_HAND, entities.getFirst().position());

        entities.getFirst().discard();
        entities.getSecond().discard();

        helper.assertTrue(state.value == 1, "Function did not increment the value!");
        helper.succeed();
    }

    @GameTest
    public void interactionRunsFunctionsWithParams(GameTestHelper helper, TestState state) {
        
        CompoundTag params = new CompoundTag();
        params.putInt("amount", 30);

        Pair<Interaction, Player> entities = spawnInteraction(helper, Map.of("databridge:increment", params), null);
        entities.getFirst().interact(entities.getSecond(), InteractionHand.MAIN_HAND, entities.getFirst().position());

        entities.getFirst().discard();
        entities.getSecond().discard();

        helper.assertTrue(state.value == 30, "Function did not increment the value!");
        helper.succeed();
    }

    @GameTest
    public void interactionRunsAttackFunctions(GameTestHelper helper, TestState state) {
                
        Pair<Interaction, Player> entities = spawnInteraction(helper, null, Map.of("databridge:increment", new CompoundTag()));
        entities.getSecond().attack(entities.getFirst());

        entities.getFirst().discard();
        entities.getSecond().discard();

        helper.assertTrue(state.value == 1, "Function did not increment the value!");
        helper.succeed();
    }

    @GameTest
    public void interactionRunsAttackFunctionsWithParams(GameTestHelper helper, TestState state) {
        
        CompoundTag params = new CompoundTag();
        params.putInt("amount", 30);
                
        Pair<Interaction, Player> entities = spawnInteraction(helper, null, Map.of("databridge:increment", params));
        entities.getSecond().attack(entities.getFirst());

        entities.getFirst().discard();
        entities.getSecond().discard();

        helper.assertTrue(state.value == 30, "Function did not increment the value!");
        helper.succeed();
    }

    @GameTest
    public void interactionSetsTriggerEntity(GameTestHelper helper, TestState state) {

        Pair<Interaction, Player> entities = spawnInteraction(helper, Map.of("databridge:trigger", new CompoundTag()), null);
        entities.getFirst().interact(entities.getSecond(), InteractionHand.MAIN_HAND, entities.getFirst().position());

        entities.getFirst().discard();
        entities.getSecond().discard();

        helper.assertTrue(state.lastEntity == entities.getSecond(), "Interaction did not set the interacting entity!");
        helper.assertTrue(state.lastTrigger == entities.getFirst(), "Interaction did not set the trigger entity!");
        helper.succeed();
    }

    @GameTest
    public void interactionSetsTriggerEntityWhenAttacking(GameTestHelper helper, TestState state) {

        Pair<Interaction, Player> entities = spawnInteraction(helper, null, Map.of("databridge:trigger", new CompoundTag()));
        entities.getSecond().attack(entities.getFirst());

        entities.getFirst().discard();
        entities.getSecond().discard();

        helper.assertTrue(state.lastEntity == entities.getSecond(), "Interaction did not set the interacting entity!");
        helper.assertTrue(state.lastTrigger == entities.getFirst(), "Interaction did not set the trigger entity!");
        helper.succeed();
    }

    @GameTest
    public void interactionSetsTriggerEntityWithParams(GameTestHelper helper, TestState state) {

        CompoundTag params = new CompoundTag();
        params.putInt("value", 30);

        Pair<Interaction, Player> entities = spawnInteraction(helper, Map.of("databridge:trigger", params), null);
        entities.getFirst().interact(entities.getSecond(), InteractionHand.MAIN_HAND, entities.getFirst().position());

        entities.getFirst().discard();
        entities.getSecond().discard();

        helper.assertTrue(state.value == 30, "Function did not set the state value!");
        helper.assertTrue(state.lastEntity == entities.getSecond(), "Interaction did not set the interacting entity!");
        helper.assertTrue(state.lastTrigger == entities.getFirst(), "Interaction did not set the trigger entity!");
        helper.succeed();
    }

    @GameTest
    public void interactionSetsTriggerEntityWhenAttackingWithParams(GameTestHelper helper, TestState state) {

        CompoundTag params = new CompoundTag();
        params.putInt("value", 30);

        Pair<Interaction, Player> entities = spawnInteraction(helper, null, Map.of("databridge:trigger", params));
        entities.getSecond().attack(entities.getFirst());

        entities.getFirst().discard();
        entities.getSecond().discard();

        helper.assertTrue(state.value == 30, "Function did not set the state value!");
        helper.assertTrue(state.lastEntity == entities.getSecond(), "Interaction did not set the interacting entity!");
        helper.assertTrue(state.lastTrigger == entities.getFirst(), "Interaction did not set the trigger entity!");
        helper.succeed();
    }
    

    // 
    // Trigger Entity Selector 
    //
    @GameTest
    public void selectorParsesTriggerEntity(GameTestHelper helper, TestState state) {
        
        String selector = "@t";
        EntitySelectorParser parser = new EntitySelectorParser(new StringReader(selector), true);

        try {
            EntitySelector sel = parser.parse();
            Interaction test = new Interaction(EntityTypes.INTERACTION, helper.getLevel());

            CommandSourceStack css = helper.getLevel().getServer().createCommandSourceStack();
            ServerFunctionUtil.setTriggerEntity(css, test);

            Entity parsed = sel.findSingleEntity(css);

            helper.assertTrue(parsed == test, "Entity selector parser did not find the trigger entity!");
            helper.succeed();

        } catch(CommandSyntaxException ex) {
            helper.fail("Failed to parse entity selector!");
        }
    }

    //
    // Permissions
    //
    @GameTest
    public void userCannotUseGamemasterCommand(GameTestHelper helper, TestState state) {
        MinecraftServer server = helper.getLevel().getServer();
        CommandSourceStack userSrc = server.createCommandSourceStack().withPermission(LevelBasedPermissionSet.NO_PERMISSIONS);

        CommandNode<CommandSourceStack> node = server.getCommands().getDispatcher().getRoot().getChild("dbalias");
        helper.assertFalse(node.canUse(userSrc), "User can use gamemaster command!");
        helper.succeed();
    }

    @GameTest
    public void gamemasterCanUseGamemasterCommand(GameTestHelper helper, TestState state) {
        MinecraftServer server = helper.getLevel().getServer();
        CommandSourceStack gamemasterSrc = server.createCommandSourceStack().withPermission(LevelBasedPermissionSet.GAMEMASTER);

        CommandNode<CommandSourceStack> node = server.getCommands().getDispatcher().getRoot().getChild("dbalias");
        helper.assertTrue(node.canUse(gamemasterSrc), "Gamemaster cannot use gamemaster command!");
        helper.succeed();
    }

    @GameTest 
    public void ownerCanUseGamemasterCommand(GameTestHelper helper, TestState state) {
        MinecraftServer server = helper.getLevel().getServer();
        CommandSourceStack ownerSrc = server.createCommandSourceStack().withPermission(LevelBasedPermissionSet.OWNER);

        CommandNode<CommandSourceStack> node = server.getCommands().getDispatcher().getRoot().getChild("dbalias");
        helper.assertTrue(node.canUse(ownerSrc), "Owner cannot use gamemaster command!");
        helper.succeed();
    }

    @GameTest
    public void userCannotUseOwnerCommand(GameTestHelper helper, TestState state) {
        MinecraftServer server = helper.getLevel().getServer();
        CommandSourceStack userSrc = server.createCommandSourceStack().withPermission(LevelBasedPermissionSet.NO_PERMISSIONS);

        CommandNode<CommandSourceStack> node = server.getCommands().getDispatcher().getRoot().getChild("high_perms");
        helper.assertFalse(node.canUse(userSrc), "User can use owner command!");
        helper.succeed();
    }

    @GameTest
    public void gamemasterCannotUseOwnerCommand(GameTestHelper helper, TestState state) {
        MinecraftServer server = helper.getLevel().getServer();
        CommandSourceStack gamemasterSrc = server.createCommandSourceStack().withPermission(LevelBasedPermissionSet.GAMEMASTER);

        CommandNode<CommandSourceStack> node = server.getCommands().getDispatcher().getRoot().getChild("high_perms");
        helper.assertFalse(node.canUse(gamemasterSrc), "Gamemaster can use owner command!");
        helper.succeed();
    }

    @GameTest 
    public void ownerCanUseOwnerCommand(GameTestHelper helper, TestState state) {
        MinecraftServer server = helper.getLevel().getServer();
        CommandSourceStack ownerSrc = server.createCommandSourceStack().withPermission(LevelBasedPermissionSet.OWNER);

        CommandNode<CommandSourceStack> node = server.getCommands().getDispatcher().getRoot().getChild("high_perms");
        helper.assertTrue(node.canUse(ownerSrc), "Owner cannot use owner command!");
        helper.succeed();
    }

    @GameTest
    public void nodeCanUseOwnerCommand(GameTestHelper helper, TestState state) {

        MinecraftServer server = helper.getLevel().getServer();
        CommandSourceStack userSrc = server.createCommandSourceStack()
            .withPermission(LevelBasedPermissionSet.NO_PERMISSIONS)
            .withEntity(new DummyPlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "Player0"))
                .withPermission("databridge.test"));

        CommandNode<CommandSourceStack> node = server.getCommands().getDispatcher().getRoot().getChild("high_perms");
        helper.assertTrue(node.canUse(userSrc), "User with node cannot use gamemaster command!");
        helper.succeed();
    }
}
