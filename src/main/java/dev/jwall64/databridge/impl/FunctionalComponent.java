package dev.jwall64.databridge.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import dev.jwall64.databridge.api.ServerFunctionUtil;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public record FunctionalComponent(Map<Action, List<FunctionRef>> functions) {

    public FunctionalComponent() {
        this(new HashMap<>());
    }

    public enum Action {
        ATTACK("attack"),
        PIERCING_ATTACK("piercing_attack"),
        INTERACT("interact"),
        INTERACT_BLOCK("interact_block"),
        INTERACT_ENTITY("interact_entity"),
        BREAK_BLOCK("break_block");

        String id;
        Action(String id) {
            this.id = id;
        }

        public String id() { return id; }
        public static Action byId(String id) {
            for(Action a : values()) {
                if(a.id.equals(id)) {
                    return a;
                }
            }
            return null;
        }

        public static Codec<Action> CODEC = Codec.STRING.xmap(Action::byId, Action::id);
    }

    private void executeFunctions(Action action, Entity player, Entity target, InteractionHand hand, CompoundTag with) {
        List<FunctionRef> list = functions.get(action);

        if(hand != null) {
            if(with == null) with = new CompoundTag();
            with.putInt("hand_index", hand.ordinal());
            with.putString("hand_slot", hand.asEquipmentSlot().getSerializedName());
        }

        if(list != null) for(FunctionRef ref : list) { 
            ref.execute(
                player.level().getServer(), 
                with, 
                css -> {
                    css = css.withEntity(player);
                    if(target != null) ServerFunctionUtil.setTriggerEntity(css, target);
                    return css;
                }
            );
        }
    }

    public void executeInteract(Entity player, InteractionHand hand) {
        executeFunctions(Action.INTERACT, player, null, hand, null);
    }

    public void executeInteractBlock(Entity player, InteractionHand hand, BlockPos block) {
        if(!functions.containsKey(Action.INTERACT_BLOCK)) return;

        CompoundTag with = new CompoundTag();
        with.putInt("block_x", block.getX());
        with.putInt("block_y", block.getY());
        with.putInt("block_z", block.getZ());
        with.putString("block_coords", block.getX() + " " + block.getY() + " " + block.getZ());
        executeFunctions(Action.INTERACT_BLOCK, player, null, hand, with);
    }

    public void executeInteractEntity(Entity player, InteractionHand hand, Entity entity) {
        executeFunctions(Action.INTERACT_ENTITY, player, entity, hand, null);
    }

    public void executePiercingAttack(Entity player) {
        executeFunctions(Action.PIERCING_ATTACK, player, null, null, null);
    }

    public void executePunchEntity(Entity player, Entity entity) {
        executeFunctions(Action.ATTACK, player, entity, null, null);
    }

    public void executeBreakBlock(Entity player, BlockPos block) {
        if(!functions.containsKey(Action.BREAK_BLOCK)) return;

        CompoundTag with = new CompoundTag();
        with.putInt("block_x", block.getX());
        with.putInt("block_y", block.getY());
        with.putInt("block_z", block.getZ());
        with.putString("block_coords", block.getX() + " " + block.getY() + " " + block.getZ());
        executeFunctions(Action.BREAK_BLOCK, player, null, null, with);
    }

    public static @Nullable FunctionalComponent get(ItemStack is) {

        CustomData customData = is.get(DataComponents.CUSTOM_DATA);
        if(customData == null) return null;

        return ((CustomDataExtension) (Object) customData).functionalComponent();
    }

    public static final Codec<FunctionalComponent> CODEC = 
        Codec.unboundedMap(Action.CODEC, FunctionRef.CODEC.listOf())
            .xmap(FunctionalComponent::new, FunctionalComponent::functions);

}
