package dev.jwall64.databridge.impl;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface CommandSourceStackExtension {

    Entity getTriggerEntity();

    void setTriggerEntity(Entity entity);

}
