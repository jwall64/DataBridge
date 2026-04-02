package dev.jwall64.databridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.jwall64.databridge.impl.FunctionalComponent;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(method="use", at=@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private void onUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> res) {
        ItemStack is = (ItemStack) (Object) this;
        FunctionalComponent cmp = FunctionalComponent.get(is);

        if(cmp == null) return;
        cmp.executeInteract(player, hand);
    }

    @Inject(method="useOn", at=@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
    private void onUseBlock(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> res) {
        ItemStack is = (ItemStack) (Object) this;
        FunctionalComponent cmp = FunctionalComponent.get(is);

        if(cmp == null) return;
        cmp.executeInteractBlock(ctx.getPlayer(), ctx.getHand(), ctx.getClickedPos());
    }

    @Inject(method="interactLivingEntity", at=@At(value="HEAD"))
    private void onInteractEntity(Player player, LivingEntity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> res) {
        
        ItemStack is = (ItemStack) (Object) this;
        FunctionalComponent cmp = FunctionalComponent.get(is);

        if(cmp == null) return;
        cmp.executeInteractEntity(player, hand, target);
    }

    @Inject(method="hurtEnemy", at=@At(value="HEAD"))
    private void onAttackEntity(LivingEntity hurt, LivingEntity attacker, CallbackInfoReturnable<Boolean> res) {
        
        ItemStack is = (ItemStack) (Object) this;
        FunctionalComponent cmp = FunctionalComponent.get(is);

        if(cmp == null) return;
        cmp.executePunchEntity(attacker, hurt);
    }


    @Inject(method="mineBlock", at=@At(value="HEAD"))
    private void onMineBlock(Level level, BlockState state, BlockPos pos, Player owner, CallbackInfo ci) {
        
        ItemStack is = (ItemStack) (Object) this;
        FunctionalComponent cmp = FunctionalComponent.get(is);

        if(cmp == null) return;
        cmp.executeBreakBlock(owner, pos);

    }

}
