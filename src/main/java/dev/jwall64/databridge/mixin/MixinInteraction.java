package dev.jwall64.databridge.mixin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.UnboundedMapCodec;

import net.minecraft.commands.*;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.jwall64.databridge.impl.CommandSourceStackExtension;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(Interaction.class)
public class MixinInteraction {

    @Unique
    private static final Logger databridge$LOGGER = LoggerFactory.getLogger("databridge");

    @Unique
    private static final Codec<List<Pair<Identifier, CompoundTag>>> databridge$FUNCTION_CODEC = new UnboundedMapCodec<>(
            Identifier.CODEC, CompoundTag.CODEC)
            .xmap(map -> {
                return map.entrySet().stream()
                        .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                        .toList();
            }, list -> {
                return list.stream()
                        .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
            });

    @Unique
    private List<Pair<Identifier, CompoundTag>> databridge$functions;

    @Unique
    private List<Pair<Identifier, CompoundTag>> databridge$attackFunctions;

    @Unique
    private CommandSource databridge$commandSource;

    @Unique
    private void executeFunctions(List<Pair<Identifier, CompoundTag>> functions, ServerPlayer player) {

        if (functions == null || functions.isEmpty())
            return;

        Interaction self = (Interaction) (Object) this;
        MinecraftServer server = ((ServerLevel) self.level()).getServer();
        if (server == null)
            return;

        CommandSourceStack cs = new CommandSourceStack(databridge$commandSource,
                self.position(),
                self.getRotationVector(),
                (ServerLevel) self.level(),
                LevelBasedPermissionSet.forLevel(PermissionLevel.GAMEMASTERS),
                self.getName().getString(), self.getName(),
                server,
                player);
        ((CommandSourceStackExtension) cs).setTriggerEntity(self);

        for (Pair<Identifier, CompoundTag> fn : functions) {
            server.getFunctions().get(fn.getFirst()).ifPresent(cf -> {
                try {
                    InstantiatedFunction<CommandSourceStack> instantiatedFunction = cf.instantiate(fn.getSecond(),
                            server.getCommands().getDispatcher());
                    Commands.executeCommandInContext(cs,
                            (executionContext) -> ExecutionContext.queueInitialFunctionCall(executionContext,
                                    instantiatedFunction, cs, CommandResultCallback.EMPTY));
                } catch (FunctionInstantiationException ignored) {
                } catch (Exception exception) {
                    databridge$LOGGER.warn("Failed to execute function {}", cf.id(), exception);
                }
            });
        }

    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(EntityType<Interaction> entityType, Level level, CallbackInfo ci) {

        databridge$commandSource = new CommandSource() {
            @Override
            public void sendSystemMessage(Component component) {
            }

            @Override
            public boolean acceptsSuccess() {
                return false;
            }

            @Override
            public boolean acceptsFailure() {
                return false;
            }

            @Override
            public boolean shouldInformAdmins() {
                return false;
            }
        };
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void onLoad(ValueInput tag, CallbackInfo ci) {
        databridge$functions = tag.read("functions", databridge$FUNCTION_CODEC).orElseGet(Collections::emptyList);
        databridge$attackFunctions = tag.read("attack_functions", databridge$FUNCTION_CODEC)
                .orElseGet(Collections::emptyList);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void onSave(ValueOutput tag, CallbackInfo ci) {

        if (databridge$functions != null && !databridge$functions.isEmpty()) {
            tag.store("functions", databridge$FUNCTION_CODEC, databridge$functions);
        }

        if (databridge$attackFunctions != null && !databridge$attackFunctions.isEmpty()) {
            tag.store("attack_functions", databridge$FUNCTION_CODEC, databridge$attackFunctions);
        }
    }

    @Inject(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Interaction$PlayerAction;<init>(Ljava/util/UUID;J)V"))
    private void onInteract(Player player, InteractionHand interactionHand,
            CallbackInfoReturnable<InteractionResult> cir) {
        executeFunctions(databridge$functions, (ServerPlayer) player);
    }

    @Inject(method = "skipAttackInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Interaction$PlayerAction;<init>(Ljava/util/UUID;J)V"))
    private void onAttack(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ServerPlayer spl) {
            executeFunctions(databridge$attackFunctions, spl);
        }
    }

}
