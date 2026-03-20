package dev.jwall64.databridge.mixin;

import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.TaskChainer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.jwall64.databridge.impl.CommandSourceStackExtension;


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

    @Inject(method="withSource", at=@At("TAIL"), cancellable=true)
    private void forwardTrigger(CommandSource arg, CallbackInfoReturnable<CommandSourceStack> cir) {
        ((CommandSourceStackExtension) cir.getReturnValue()).setTriggerEntity(databridge$trigger);
    }

    @Inject(method="withEntity", at=@At("TAIL"), cancellable=true)
    private void forwardTrigger(Entity arg, CallbackInfoReturnable<CommandSourceStack> cir) {
        ((CommandSourceStackExtension) cir.getReturnValue()).setTriggerEntity(databridge$trigger);
    }

    @Inject(method="withPosition", at=@At("TAIL"), cancellable=true)
    private void forwardTrigger(Vec3 arg, CallbackInfoReturnable<CommandSourceStack> cir) {
        ((CommandSourceStackExtension) cir.getReturnValue()).setTriggerEntity(databridge$trigger);
    }

    @Inject(method="withRotation", at=@At("TAIL"), cancellable=true)
    private void forwardTrigger(Vec2 arg, CallbackInfoReturnable<CommandSourceStack> cir) {
        ((CommandSourceStackExtension) cir.getReturnValue()).setTriggerEntity(databridge$trigger);
    }

    @Inject(method="withCallback(Lnet/minecraft/commands/CommandResultCallback;)Lnet/minecraft/commands/CommandSourceStack;", at=@At("TAIL"), cancellable=true)
    private void forwardTrigger(CommandResultCallback arg, CallbackInfoReturnable<CommandSourceStack> cir) {
        ((CommandSourceStackExtension) cir.getReturnValue()).setTriggerEntity(databridge$trigger);
    }

    @Inject(method="withSuppressedOutput", at=@At("TAIL"), cancellable=true)
    private void forwardTrigger(CallbackInfoReturnable<CommandSourceStack> cir) {
        ((CommandSourceStackExtension) cir.getReturnValue()).setTriggerEntity(databridge$trigger);
    }

    @Inject(method="withPermission", at=@At("TAIL"), cancellable=true)
    private void forwardTrigger(PermissionSet arg, CallbackInfoReturnable<CommandSourceStack> cir) {
        ((CommandSourceStackExtension) cir.getReturnValue()).setTriggerEntity(databridge$trigger);
    }

    @Inject(method="withMaximumPermission", at=@At("TAIL"), cancellable=true)
    private void forwardTriggerMaxPermission(PermissionSet arg, CallbackInfoReturnable<CommandSourceStack> cir) {
        ((CommandSourceStackExtension) cir.getReturnValue()).setTriggerEntity(databridge$trigger);
    }

    @Inject(method="withAnchor", at=@At("TAIL"), cancellable=true)
    private void forwardTrigger(EntityAnchorArgument.Anchor arg, CallbackInfoReturnable<CommandSourceStack> cir) {
        ((CommandSourceStackExtension) cir.getReturnValue()).setTriggerEntity(databridge$trigger);
    }

    @Inject(method="withLevel", at=@At("TAIL"), cancellable=true)
    private void forwardTrigger(ServerLevel arg, CallbackInfoReturnable<CommandSourceStack> cir) {
        ((CommandSourceStackExtension) cir.getReturnValue()).setTriggerEntity(databridge$trigger);
    }

    @Inject(method="facing(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/commands/CommandSourceStack;", at=@At("TAIL"), cancellable=true)
    private void forwardTriggerFacing(Vec3 arg, CallbackInfoReturnable<CommandSourceStack> cir) {
        ((CommandSourceStackExtension) cir.getReturnValue()).setTriggerEntity(databridge$trigger);
    }

    @Inject(method="withSigningContext", at=@At("TAIL"), cancellable=true)
    private void forwardTrigger(CommandSigningContext arg, TaskChainer arg1, CallbackInfoReturnable<CommandSourceStack> cir) {
        ((CommandSourceStackExtension) cir.getReturnValue()).setTriggerEntity(databridge$trigger);
    }


}
