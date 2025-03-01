package dev.jwall64.databridge;

import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

public class TestState {

    public int value = 0;


    public static TestState create(ReloadableServerResources resources, ReloadableServerRegistries.LoadResult loadResult, ResourceManager resourceManager, @Nullable TestState previous) {
        if(previous != null) return previous;
        return new TestState();
    }

}
