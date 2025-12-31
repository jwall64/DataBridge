package dev.jwall64.databridge.test;

import java.util.HashSet;
import java.util.Set;

import com.mojang.authlib.GameProfile;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

public class DummyPlayer extends Player {


    private final Set<String> permissions = new HashSet<>();

    public DummyPlayer(Level level, GameProfile profile) {
        super(level, profile);
    }

    public GameType gameMode() {
        return GameType.SURVIVAL;
    }

    public DummyPlayer withPermission(String permission) {
        permissions.add(permission);
        return this;
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
}
