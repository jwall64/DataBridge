package dev.jwall64.databridge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.advancements.predicates.MinMaxBounds.Doubles;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.jwall64.databridge.impl.EntitySelectorExtension;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;

@Mixin(EntitySelectorParser.class)
public abstract class MixinEntitySelectorParser {

    @Shadow private int maxResults;
    @Shadow private BiConsumer<Vec3, List<? extends Entity>> order;
    @Shadow private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions;

    @Shadow protected abstract CompletableFuture<Suggestions> suggestOpenOptions(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer);

    @Shadow @Final private List<Predicate<Entity>> predicates;
    @Shadow @Final private StringReader reader;

    @Shadow protected abstract CompletableFuture<Suggestions> suggestOptionsKeyOrClose(SuggestionsBuilder suggestionsBuilder, Consumer<SuggestionsBuilder> consumer);

    @Shadow protected abstract void parseOptions() throws CommandSyntaxException;

    @Shadow private boolean includesEntities;
    @Unique
    private boolean databridge$triggerEntity;

    @Inject(method="parseSelector", at=@At(value="INVOKE", target="Lcom/mojang/brigadier/StringReader;setCursor(I)V"), cancellable=true)
    private void redirectInvalidSelector(CallbackInfo ci, @Local char c) throws CommandSyntaxException {

        if(c == 't') {
            maxResults = 1;
            order = EntitySelector.ORDER_ARBITRARY;
            databridge$triggerEntity = true;
            includesEntities = true;
            predicates.add(Entity::isAlive);
            suggestions = this::suggestOpenOptions;

            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.reader.skip();
                this.suggestions = this::suggestOptionsKeyOrClose;
                this.parseOptions();
            }

            ci.cancel();
        }

    }



    @WrapOperation(method="getSelector", at=@At(value="NEW", target="Lnet/minecraft/commands/arguments/selector/EntitySelector;<init>(IZZLjava/util/List;Lnet/minecraft/advancements/predicates/MinMaxBounds$Doubles;Ljava/util/function/Function;Lnet/minecraft/world/phys/AABB;Ljava/util/function/BiConsumer;ZLjava/lang/String;Ljava/util/UUID;Lnet/minecraft/world/entity/EntityType;Z)Lnet/minecraft/commands/arguments/selector/EntitySelector;"))
    private EntitySelector wrapCreateSelector(int maxResults, boolean includeEntities, boolean worldLimited,
                                              List<Predicate<Entity>> predicates, @Nullable Doubles distance,
                                              Function<Vec3, Vec3> function, @Nullable AABB bounds, BiConsumer<Vec3, List<? extends Entity>> order,
                                              boolean currentEntity, @Nullable String playerName, @Nullable UUID uuid,
                                              @Nullable EntityType<?> type, boolean usesSelectors, Operation<EntitySelector> original) {

        EntitySelector out = original.call(maxResults, includeEntities, worldLimited, predicates, distance, function,
                bounds, order, currentEntity, playerName, uuid, type, usesSelectors);

        ((EntitySelectorExtension) out).setTriggerEntity(databridge$triggerEntity);

        return out;
    }


}
