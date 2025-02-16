package dev.jwall64.databridge.mixin;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import dev.jwall64.databridge.JavaFunctionDefinition;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Mixin(ServerFunctionLibrary.class)
public class MixinServerFunctionLibrary {

    @Shadow @Final public static ResourceKey<Registry<CommandFunction<CommandSourceStack>>> TYPE_KEY;
    @Unique
    private final FileToIdConverter databridge$lister =  FileToIdConverter.json(Registries.elementsDirPath(TYPE_KEY));

    @ModifyVariable(method="reload", at=@At(value="STORE"), ordinal=1)
    private CompletableFuture<Map<ResourceLocation, CompletableFuture<CommandFunction<CommandSourceStack>>>> modifyFunctionMap(
            CompletableFuture<Map<ResourceLocation, CompletableFuture<CommandFunction<CommandSourceStack>>>> og,
            PreparableReloadListener.PreparationBarrier preparationBarrier,
            ResourceManager resourceManager,
            Executor executor,
            Executor executor2) {

        return og.thenCompose(map -> {
            Map<ResourceLocation, Resource> javaFunctions = databridge$lister.listMatchingResources(resourceManager);
            for(Map.Entry<ResourceLocation, Resource> entry : javaFunctions.entrySet()) {
                ResourceLocation location = entry.getKey();
                ResourceLocation id = databridge$lister.fileToId(location);

                Resource resource = entry.getValue();

                map.put(id, CompletableFuture.supplyAsync(() -> {

                    Gson gson = new GsonBuilder().create();
                    JsonObject obj;

                    try(BufferedReader reader = new BufferedReader(resource.openAsReader())) {
                        obj = gson.fromJson(reader, JsonObject.class);
                    } catch (IOException | JsonIOException | JsonSyntaxException ex) {
                        throw new CompletionException(ex);
                    }

                    JavaFunctionDefinition def = JavaFunctionDefinition.CODEC.decode(JsonOps.INSTANCE, obj).getOrThrow().getFirst();
                    return def.getFunction(id);

                }, executor));
            }

            CompletableFuture<?>[] completableFutures = (CompletableFuture<?>[]) map.values().toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(completableFutures).handle((void_, th) -> map);
        });
    }

}

