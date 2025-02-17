package dev.jwall64.databridge.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import dev.jwall64.databridge.JavaFunctionDefinition;
import dev.jwall64.databridge.ServerFunctionLibraryExtension;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ServerFunctionLibrary.class)
@Implements(@Interface(iface=ServerFunctionLibraryExtension.class, prefix="databridge$"))
public class MixinServerFunctionLibrary {

    @Unique
    private IdMap<Holder<JavaFunctionDefinition>> databridge$javaFunctions;

    @Unique
    public void databridge$addJavaFunctions(Registry<JavaFunctionDefinition> registry) {
        databridge$javaFunctions = registry.asHolderIdMap();
    }


    @ModifyVariable(method="reload", at=@At(value="STORE"), ordinal=1)
    private CompletableFuture<Map<ResourceLocation, CompletableFuture<CommandFunction<CommandSourceStack>>>> modifyFunctionMap(
            CompletableFuture<Map<ResourceLocation, CompletableFuture<CommandFunction<CommandSourceStack>>>> og,
            PreparableReloadListener.PreparationBarrier preparationBarrier,
            ResourceManager resourceManager,
            Executor executor,
            Executor executor2) {

        if(databridge$javaFunctions == null || databridge$javaFunctions.size() == 0) {
            return og;
        }

        return og.thenCompose(map -> {
            for(Holder<JavaFunctionDefinition> entry : databridge$javaFunctions) {
                ResourceLocation id = entry.unwrapKey().orElseThrow().location();
                map.put(id, CompletableFuture.supplyAsync(() -> entry.value().getFunction(id), executor));
            }
            CompletableFuture<?>[] completableFutures = (CompletableFuture<?>[]) map.values().toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(completableFutures).handle((void_, th) -> map);
        });
    }

}
