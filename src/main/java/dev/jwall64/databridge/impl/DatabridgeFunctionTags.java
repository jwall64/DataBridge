package dev.jwall64.databridge.impl;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class DatabridgeFunctionTags {

    public static final Identifier JOIN   = Identifier.tryBuild("databridge", "join");
    public static final Identifier JOINED = Identifier.tryBuild("databridge", "joined");
    public static final Identifier LEAVE  = Identifier.tryBuild("databridge", "leave");

}
