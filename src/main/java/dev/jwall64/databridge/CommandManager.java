package dev.jwall64.databridge;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public class CommandManager implements PreparableReloadListener {

    public static final ResourceKey<Registry<CommandFunction<CommandSourceStack>>> TYPE_KEY = ResourceKey.createRegistryKey(ResourceLocation.withDefaultNamespace("command"));
    private static final FileToIdConverter LISTER = FileToIdConverter.json(Registries.elementsDirPath(TYPE_KEY));
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandManager.class);

    private Map<String, LiteralArgumentBuilder<CommandSourceStack>> loadedCommands;

    public void registerAndForget(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (Map.Entry<String, LiteralArgumentBuilder<CommandSourceStack>> ent : loadedCommands.entrySet()) {
            dispatcher.register(ent.getValue());
        }
        loadedCommands = Maps.newHashMap();
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor executor, Executor executor2) {

        CompletableFuture<Map<ResourceLocation, CompletableFuture<LiteralArgumentBuilder<CommandSourceStack>>>> allCommands =
                CompletableFuture
                        .supplyAsync(() -> LISTER.listMatchingResources(resourceManager), executor)
                        .thenCompose(map -> {

            Map<ResourceLocation, CompletableFuture<LiteralArgumentBuilder<CommandSourceStack>>> commands = Maps.newHashMap();
            for(Map.Entry<ResourceLocation, Resource> entry : map.entrySet()) {

                ResourceLocation location = entry.getKey();
                ResourceLocation id = LISTER.fileToId(location);

                commands.putIfAbsent(id, CompletableFuture.supplyAsync(() -> {

                    Gson gson = new GsonBuilder().create();
                    JsonObject obj;
                    try(BufferedReader reader = new BufferedReader(entry.getValue().openAsReader())) {
                        obj = gson.fromJson(reader, JsonObject.class);
                    } catch (IOException | JsonIOException | JsonSyntaxException ex) {
                        throw new CompletionException(ex);
                    }

                    CommandDefinition def = CommandDefinition.CODEC.decode(JsonOps.INSTANCE, obj).getOrThrow().getFirst();
                    return def.create();

                }, executor));

            }

            CompletableFuture<?>[] completableFutures = (CompletableFuture<?>[]) commands.values().toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(completableFutures).handle((void_, throwable) -> commands);
        });

        return allCommands.thenCompose(preparationBarrier::wait).thenAcceptAsync(map -> {

            ImmutableMap.Builder<String, LiteralArgumentBuilder<CommandSourceStack>> builder = ImmutableMap.builder();
            map.forEach((id, future) -> future.handle((literal, throwable) -> {
                if (throwable != null) {
                    LOGGER.error("Failed to load command {}", id, throwable);
                } else {
                    builder.put(id.getPath(), literal);
                }
                return null;
            }).join());

            this.loadedCommands = builder.build();

        }, executor2);

    }
}
