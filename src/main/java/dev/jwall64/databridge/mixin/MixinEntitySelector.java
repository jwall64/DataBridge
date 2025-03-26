package dev.jwall64.databridge.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.jwall64.databridge.impl.CommandSourceStackExtension;
import dev.jwall64.databridge.impl.EntitySelectorExtension;

import java.util.List;

@Mixin(EntitySelector.class)
@Implements(@Interface(iface = EntitySelectorExtension.class, prefix = "databridge$"))
public class MixinEntitySelector {

    @Unique
    private boolean databridge$triggerEntity = false;

    public void databridge$setTriggerEntity(boolean triggerEntity) {
        this.databridge$triggerEntity = triggerEntity;
    }

    public boolean databridge$isTriggerEntity() {
        return this.databridge$triggerEntity;
    }

    @Inject(method="findEntities", at=@At("HEAD"), cancellable = true)
    private void injectFind(CommandSourceStack commandSourceStack, CallbackInfoReturnable<List<? extends Entity>> cir) {
        if(databridge$isTriggerEntity()) {
            cir.setReturnValue(List.of(((CommandSourceStackExtension) commandSourceStack).getTriggerEntity()));
        }
    }

}