package dev.jwall64.databridge.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import dev.jwall64.databridge.impl.CustomClickEventHandler;
import dev.jwall64.databridge.impl.DataBridgeRegistries;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder.Reference;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListener extends ServerCommonPacketListenerImpl {

    @Shadow
	public ServerPlayer player;

    public MixinServerGamePacketListener(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }

    @Override
	public void handleCustomClickAction(ServerboundCustomClickActionPacket serverboundCustomClickActionPacket) {

        PacketUtils.ensureRunningOnSameThread(serverboundCustomClickActionPacket, this, this.player.level());

        RegistryAccess access = player.registryAccess();

        Optional<Reference<CustomClickEventHandler>> handler = 
            access.lookupOrThrow(DataBridgeRegistries.CLICK_EVENT_HANDLER).get(ResourceKey.create(DataBridgeRegistries.CLICK_EVENT_HANDLER, serverboundCustomClickActionPacket.id()));

        handler.ifPresent(ref -> {
            ref.value().handle(player, serverboundCustomClickActionPacket.payload().orElse(null));
        });

        super.handleCustomClickAction(serverboundCustomClickActionPacket);
    }

}
