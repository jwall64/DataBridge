package dev.jwall64.databridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.jwall64.databridge.impl.FunctionalComponent;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.PiercingWeapon;

@Mixin(PiercingWeapon.class)
public class MixinPiercingWeapon {

    @Inject(method="attack", at=@At("HEAD"))
    private void onAttack(LivingEntity attacker, EquipmentSlot hand, CallbackInfo ci) {

        ItemStack is = attacker.getItemBySlot(hand);
        if(is == null) return;

        FunctionalComponent cmp = FunctionalComponent.get(is);
        if(cmp == null) return;

        cmp.executePiercingAttack(attacker);

    }

}
