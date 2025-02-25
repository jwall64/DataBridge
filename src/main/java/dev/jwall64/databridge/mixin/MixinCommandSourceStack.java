package dev.jwall64.databridge.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import dev.jwall64.databridge.CommandSourceStackExtension;


@Mixin(CommandSourceStack.class)
@Implements(@Interface(iface = CommandSourceStackExtension.class, prefix = "databridge$"))
public class MixinCommandSourceStack {

    @Unique
    @Nullable
    private Entity databridge$trigger;

    public void databridge$setTriggerEntity(Entity entity) {
        this.databridge$trigger = entity;
    }

    public Entity databridge$getTriggerEntity() {
        return this.databridge$trigger;
    }


}
