package dev.jwall64.databridge.test;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;

public class TestState {

    private static final Logger log = LoggerFactory.getLogger(TestState.class);

    public int value = 0;
    public Entity lastEntity;
    public Entity lastTrigger;

    public static TestState create(MinecraftServer server, @Nullable TestState previous) {
        if(previous == null) {

            PermissionCheckEvent.EVENT.register((src, permission) -> {
                log.info("Checking permission {} for {}", permission, src);
                if(src instanceof CommandSourceStack css 
                    && css.getEntity() instanceof DummyPlayer dp 
                    && dp.hasPermission(permission)) {

                    return TriState.TRUE;
                }
                return TriState.DEFAULT;
            });

        }
        log.info("Creating new TestState");
        return new TestState();
    }

    public static void cleanup(MinecraftServer server, TestState state) {
        log.info("Cleaning up TestState {}", state.value);
    }

}
