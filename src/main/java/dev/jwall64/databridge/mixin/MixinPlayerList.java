package dev.jwall64.databridge.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.jwall64.databridge.api.ServerFunctionUtil;
import dev.jwall64.databridge.impl.DatabridgeFunctionTags;

@Mixin(PlayerList.class)
public class MixinPlayerList {

    @Inject(method="placeNewPlayer", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void onJoin(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        CompoundTag data = new CompoundTag();
        data.putString("username", serverPlayer.getGameProfile().getName());
        data.putString("uuid", serverPlayer.getUUID().toString());
        ServerFunctionUtil.executeFunctionTag(serverPlayer.server, DatabridgeFunctionTags.JOIN, data, source -> source.withEntity(serverPlayer));
    }

    @Inject(method="remove", at=@At(value="HEAD"))
    private void onJoin(ServerPlayer serverPlayer, CallbackInfo ci) {
        CompoundTag data = new CompoundTag();
        data.putString("username", serverPlayer.getGameProfile().getName());
        data.putString("uuid", serverPlayer.getUUID().toString());
        ServerFunctionUtil.executeFunctionTag(serverPlayer.server, DatabridgeFunctionTags.LEAVE, data, source -> source.withEntity(serverPlayer));
    }

}
