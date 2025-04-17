package dev.jwall64.databridge;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestState {

    private static final Logger log = LoggerFactory.getLogger(TestState.class);
    public int value = 0;


    public static TestState create(MinecraftServer server, @Nullable TestState previous) {
        log.info("Creating new TestState");
        return new TestState();
    }

    public static void cleanup(MinecraftServer server, TestState state) {
        log.info("Cleaning up TestState {}", state.value);
    }

}
