package dev.jwall64.databridge.impl;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

public record CustomClickEventHandler(FunctionRef function) {

    public void handle(ServerPlayer sender, @Nullable Tag data) {

        CompoundTag with = null;
        if(data instanceof CompoundTag ct) with = ct;
        else if(data != null) {
            with = new CompoundTag();
            with.put("value", data);
        }

        function.execute(sender.level().getServer(), with, css -> css.withEntity(sender));
    }

    public static final Codec<CustomClickEventHandler> CODEC = 
            FunctionRef.CODEC
                .xmap(CustomClickEventHandler::new, CustomClickEventHandler::function)
                .fieldOf("function")
                .codec();
}
