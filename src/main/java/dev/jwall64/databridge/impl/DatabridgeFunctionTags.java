package dev.jwall64.databridge.impl;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class DatabridgeFunctionTags {

    public static final Identifier JOIN = Identifier.withDefaultNamespace("join");
    public static final Identifier JOINED = Identifier.withDefaultNamespace("joined");
    public static final Identifier LEAVE= Identifier.withDefaultNamespace("leave");

}
