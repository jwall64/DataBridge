package dev.jwall64.databridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.jwall64.databridge.impl.FunctionalComponent;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @WrapOperation(method="use", at=@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult wrapUse(Item item, Level level, Player player, InteractionHand hand, Operation<InteractionResult> orig) {

        InteractionResult result = orig.call(item, level, player, hand);
        if(result.consumesAction() || level.isClientSide()) {
            return result;
        }

        ItemStack is = (ItemStack) (Object) this;
        FunctionalComponent cmp = FunctionalComponent.get(is);
        if(cmp == null) return InteractionResult.PASS;
        
        return cmp.executeInteract(player, hand);
    }

    @WrapOperation(method="useOn", at=@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult wrapUseBlock(Item item, UseOnContext ctx, Operation<InteractionResult> orig) {

        InteractionResult result = orig.call(item, ctx);
        if(result.consumesAction() || ctx.getLevel().isClientSide()) {
            return result;
        }

        ItemStack is = (ItemStack) (Object) this;
        FunctionalComponent cmp = FunctionalComponent.get(is);

        if(cmp == null) return InteractionResult.PASS;

        return cmp.executeInteractBlock(ctx.getPlayer(), ctx.getHand(), ctx.getClickedPos());
    }

    @WrapOperation(method="interactLivingEntity", at=@At(value="INVOKE", target="Lnet/minecraft/world/item/Item;interactLivingEntity(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult onInteractEntity(Item item, ItemStack self, Player player, LivingEntity target, InteractionHand hand, Operation<InteractionResult> orig) {

        InteractionResult res = orig.call(item, self, player, target, hand);
        if(res.consumesAction() || player.level().isClientSide()) {
            return res;
        }

        FunctionalComponent cmp = FunctionalComponent.get(self);
        if(cmp == null) return InteractionResult.PASS;

        return cmp.executeInteractEntity(player, hand, target);
    }

    @Inject(method="hurtEnemy", at=@At(value="HEAD"))
    private void onAttackEntity(LivingEntity hurt, LivingEntity attacker, CallbackInfoReturnable<Boolean> res) {
        
        if(attacker.level().isClientSide()) return;

        ItemStack is = (ItemStack) (Object) this;
        FunctionalComponent cmp = FunctionalComponent.get(is);

        if(cmp == null) return;
        cmp.executePunchEntity(attacker, hurt);
    }


    @Inject(method="mineBlock", at=@At(value="HEAD"))
    private void onMineBlock(Level level, BlockState state, BlockPos pos, Player owner, CallbackInfo ci) {
        
        if(level.isClientSide()) return;

        ItemStack is = (ItemStack) (Object) this;
        FunctionalComponent cmp = FunctionalComponent.get(is);

        if(cmp == null) return;
        cmp.executeBreakBlock(owner, pos);

    }

}
