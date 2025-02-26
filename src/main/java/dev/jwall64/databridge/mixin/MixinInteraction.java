package dev.jwall64.databridge.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.*;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.jwall64.databridge.CommandSourceStackExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(Interaction.class)
public class MixinInteraction {

    @Shadow @Final private static Logger LOGGER;

    @Unique
    private List<Pair<ResourceLocation, CompoundTag>> databridge$functions;

    @Unique
    private List<Pair<ResourceLocation, CompoundTag>> databridge$attackFunctions;

    @Unique
    private CommandSource databridge$commandSource;

    @Unique
    private List<Pair<ResourceLocation, CompoundTag>> databridge$readFunctions(CompoundTag tag) {
        List<Pair<ResourceLocation, CompoundTag>> readFunctions = new ArrayList<>();
        for(String key : tag.getAllKeys()) {

            CompoundTag args = new CompoundTag();
            Tag t = tag.get(key);
            if(t instanceof CompoundTag ct) {
                args = ct;
            }

            readFunctions.add(Pair.of(ResourceLocation.tryParse(key), args));
        }
        return List.copyOf(readFunctions);
    }

    @Unique
    private void executeFunctions(List<Pair<ResourceLocation, CompoundTag>> functions, ServerPlayer player) {

        if(functions == null || functions.isEmpty()) return;

        Interaction self = (Interaction) (Object) this;
        MinecraftServer server = self.getServer();
        if(server == null) return;

        CommandSourceStack cs = new CommandSourceStack(databridge$commandSource,
                self.position(),
                self.getRotationVector(),
                (ServerLevel) self.level(),
                2,
                self.getName().getString(), self.getName(),
                server,
                player);
        ((CommandSourceStackExtension) cs).setTriggerEntity(self);

        for(Pair<ResourceLocation, CompoundTag> fn : functions) {
            server.getFunctions().get(fn.getFirst()).ifPresent(cf -> {
                try {
                    InstantiatedFunction<CommandSourceStack> instantiatedFunction = cf.instantiate(fn.getSecond(), server.getCommands().getDispatcher());
                    Commands.executeCommandInContext(cs, (executionContext) -> ExecutionContext.queueInitialFunctionCall(executionContext, instantiatedFunction, cs, CommandResultCallback.EMPTY));
                } catch (FunctionInstantiationException ignored) {
                } catch (Exception exception) {
                    LOGGER.warn("Failed to execute function {}", cf.id(), exception);
                }
            });
        }

    }

    @Inject(method="<init>", at=@At("TAIL"))
    private void onInit(EntityType<Interaction> entityType, Level level, CallbackInfo ci) {

        databridge$commandSource = new CommandSource() {
            @Override
            public void sendSystemMessage(Component component) { }
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

    @Inject(method="readAdditionalSaveData", at=@At("RETURN"))
    private void onLoad(CompoundTag tag, CallbackInfo ci) {
        if(tag.contains("functions", Tag.TAG_COMPOUND)) {
            databridge$functions = databridge$readFunctions(tag.getCompound("functions"));
        } else {
            databridge$functions = Collections.emptyList();
        }
        if(tag.contains("attack_functions", Tag.TAG_COMPOUND)) {
            databridge$attackFunctions = databridge$readFunctions(tag.getCompound("attack_functions"));
        } else {
            databridge$attackFunctions = Collections.emptyList();
        }
    }

    @Inject(method="addAdditionalSaveData", at=@At("RETURN"))
    private void onSave(CompoundTag tag, CallbackInfo ci) {
        if (databridge$functions != null && !databridge$functions.isEmpty()) {
            CompoundTag functions = new CompoundTag();
            for (Pair<ResourceLocation, CompoundTag> fn : databridge$functions) {
                functions.put(fn.getFirst().toString(), fn.getSecond());
            }
            tag.put("functions", functions);
        }
        if (databridge$attackFunctions != null && !databridge$attackFunctions.isEmpty()) {
            CompoundTag functions = new CompoundTag();
            for (Pair<ResourceLocation, CompoundTag> fn : databridge$attackFunctions) {
                functions.put(fn.getFirst().toString(), fn.getSecond());
            }
            tag.put("attack_functions", functions);
        }
    }

    @Inject(method="interact", at=@At(value = "INVOKE", target="Lnet/minecraft/world/entity/Interaction$PlayerAction;<init>(Ljava/util/UUID;J)V"))
    private void onInteract(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        executeFunctions(databridge$functions, (ServerPlayer) player);
    }

    @Inject(method="skipAttackInteraction", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/Interaction$PlayerAction;<init>(Ljava/util/UUID;J)V"))
    private void onAttack(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(entity instanceof ServerPlayer spl) {
            executeFunctions(databridge$attackFunctions, spl);
        }
    }


}
