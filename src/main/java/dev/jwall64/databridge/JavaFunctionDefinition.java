package dev.jwall64.databridge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public record JavaFunctionDefinition(Type type, String reference, Optional<Holder<StateObject<?>>> stateObject) {

    public static final Codec<JavaFunctionDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Type.CODEC.optionalFieldOf("type", Type.METHOD).forGetter(JavaFunctionDefinition::type),
            Codec.STRING.fieldOf("value").forGetter(JavaFunctionDefinition::reference),
            RegistryFixedCodec.create(DataBridgeRegistries.STATE_OBJECT).optionalFieldOf("state_object").forGetter(JavaFunctionDefinition::stateObject)
    ).apply(instance, JavaFunctionDefinition::new));

    @SuppressWarnings("unchecked")
    public CommandFunction<CommandSourceStack> getFunction(ResourceLocation id) {

        StateObject<?> obj = stateObject.isPresent() ? stateObject.orElseThrow().value() : StateObject.EMPTY;
        if(type == Type.METHOD) {
            try {

                MethodHandle method = Utils.findMethod(reference, void.class,
                        CommandSourceStack.class,
                        CompoundTag.class,
                        ResourceLocation.class,
                        CommandDispatcher.class,
                        ExecutionContext.class,
                        Frame.class,
                        obj.type());

                return new JavaFunction(id, method, obj.value());

            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        } else {

            try {
                Class<?> clazz = Class.forName(reference);
                if(!CommandFunction.class.isAssignableFrom(clazz)) {
                    throw new IllegalArgumentException("Expected a class which implements CommandFunction!");
                }
                Constructor<?> constructor = clazz.getConstructor(ResourceLocation.class, obj.type());
                return (CommandFunction<CommandSourceStack>) constructor.newInstance(id, obj.value());

            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException ex) {
                throw new RuntimeException(ex);

            }

        }
    }

    enum Type {
        METHOD("method"),
        OBJECT("object");

        private final String id;
        Type(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static Type byId(String id) {
            for (Type t : Type.values()) {
                if (t.id.equals(id)) {
                    return t;
                }
            }
            return null;
        }

        public static final Codec<Type> CODEC = Codec.STRING.xmap(Type::byId, Type::getId);
    }

}
