package dev.jwall64.databridge.impl;

import org.jspecify.annotations.Nullable;
import dev.jwall64.databridge.api.ServerFunctionUtil;

import com.mojang.serialization.Codec;

import net.minecraft.commands.CommandResultCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record CustomClickEventHandler(Identifier function) {

    public void handle(ServerPlayer sender, @Nullable Tag data) {

        CompoundTag with = null;
        if(data instanceof CompoundTag ct) with = ct;
        else if(data != null) {
            with = new CompoundTag();
            with.put("value", data);
        }

        ServerFunctionUtil.executeFunction(
            sender.level().getServer(),
            function, 
            with, 
            css -> css.withEntity(sender),
            CommandResultCallback.EMPTY
        );
    }

    public static final Codec<CustomClickEventHandler> CODEC = 
            Identifier.CODEC
                .xmap(CustomClickEventHandler::new, CustomClickEventHandler::function)
                .fieldOf("function")
                .codec();
}
