package dev.jwall64.databridge.mixin;

import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;

@Mixin(ExecutionContext.class)
public interface AccessorExecutionContext {

    @Invoker("createTopFrame")
    static <T extends ExecutionCommandSource<T>> Frame callCreateTopFrame(ExecutionContext<T> ctx, CommandResultCallback callback) {
        throw new NotImplementedException();
    }
}
