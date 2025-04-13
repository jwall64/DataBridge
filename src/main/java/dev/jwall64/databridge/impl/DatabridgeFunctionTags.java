package dev.jwall64.databridge.impl;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class DatabridgeFunctionTags {

    public static final ResourceLocation JOIN = ResourceLocation.withDefaultNamespace("join");
    public static final ResourceLocation JOINED = ResourceLocation.withDefaultNamespace("joined");
    public static final ResourceLocation LEAVE= ResourceLocation.withDefaultNamespace("leave");

}
