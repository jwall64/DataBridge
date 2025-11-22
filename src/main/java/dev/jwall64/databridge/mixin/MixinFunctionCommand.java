package dev.jwall64.databridge.mixin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.commands.*;

@Mixin(FunctionCommand.class)
public class MixinFunctionCommand {

    @Redirect(method = "method_13382", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerFunctionManager;getFunctionNames()Ljava/lang/Iterable;"))
    private static Iterable<ResourceLocation> redirectNames(ServerFunctionManager manager) {

        List<ResourceLocation> out = new ArrayList<>();
        Iterator<ResourceLocation> it = manager.getFunctionNames().iterator();
        while (it.hasNext()) {
            ResourceLocation loc = it.next();
            if (loc.getPath().charAt(0) != '_') {
                out.add(loc);
            }
        }

        return out;
    }

}
