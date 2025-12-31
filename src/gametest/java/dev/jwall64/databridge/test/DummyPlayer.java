package dev.jwall64.databridge.test;

import com.mojang.authlib.GameProfile;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

public class DummyPlayer extends Player {

    public DummyPlayer(Level level, GameProfile profile) {
        super(level, profile);
    }

    public GameType gameMode() {
        return GameType.SURVIVAL;
    }

}
