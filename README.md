## DataBridge

An attempt to bridge the gap between data packs and mods.

### Java Functions
With this mod installed, data packs can register functions that point to Java code. To do this, create a `.json` file
in the pack's `function` folder with the following format:
```json
{
  "type": "",
  "value": "",
  "state_object": null
}
```
The `type` field can be either `method` or `object`.<br/>
The `state_object` field is optional. If present, it is the name of a state object. (See below)<br/>

If the type is `method`, `value` should be a method reference in the
form `<fully.qualified.class.Name>::<method>`. The method with that name should have the following signature:
```java
public static void method(CommandSourceStack css,
            CompoundTag tag,
            ResourceLocation id,
            CommandDispatcher<CommandSourceStack> dispatcher,
            ExecutionContext<CommandSourceStack> ctx,
            Frame frame,
            S state) { }
```
If no state object is specified, The state object type is Void.

If the type is `object`, `value` should be method reference in the
form `<fully.qualified.class.Name>::<method>`. The method with that name should have the following signature:
```java
public static CommandFunction<CommandSourceStack> method(ResourceLocation id, Supplier<S> state);
```

### Command Definitions
This mod also allows data packs to define commands. Commands can either be aliases to existing commands, or pointers
to Java code. Commands are registered in the `command` folder in data packs. Commands are `.json` files with the following format:
```json
{
  "name": "mycommand",
  "type": "",
  "value": "",
  "permission_level": 0,
  "permission_node": null,
  "state_object": null
}
```
The `name` field is the name of the command.<br/>
The `type` field can be `alias`, `method`, or `builder`.
The `permission_level` field is optional, and defaults to 0. This is the operator level a player needs to run the command.<br/>
The `permission_node` field is optional. If specified, players will need that permission node (or the specified permission level)
The `state_object` field is optional. If present, it is the name of a state object. (See below)<br/>
to execute the command.<br/>

If the type is `alias`, `value` should be a command string to be executed when the command is run. Command aliases will 
be executed with the server's function permission level.

For example:
```json
{
  "name": "spawn",
  "type": "alias",
  "value": "tp 0 64 0"
}
```

If the type is `method`, `value` should be a method reference in the
form `<fully.qualified.class.Name>::<method>`. The method with that name should have the following signature:
```java
public static int method(
        CommandContext<CommandSourceStack> context, 
        S state) { }
```

If the type is `builder`, `value` should be a method reference in the
form `<fully.qualified.class.Name>::<method>`. The method with that name should have the following signature:
```java
public static LiteralArgumentBuilder<CommandSourceStack> method(
        String id, 
        LiteralArgumentBuilder<CommandSourceStack> builder, 
        CommandBuildContext ctx,
        Supplier<S> state) { }
```

### State Objects
For sharing state between functions/commands, data packs can define state objects. To do so, put a json file in the `state_object`
folder with the following format:
```json
{
  "type": "<fully.qualified.class.Name>",
  "factory": "<fully.qualified.class.Name>::<method>",
  "destructor": "<fully.qualified.class.Name>::<method>"
}
```
`factory` should be a reference to a public method with the following signature:
```java
public static S method(MinecraftServer server,
                       @Nullable S prevInstance);
```
Whenever data packs are reloaded, this method will be called, The value it returns should be an instance of `type` and is 
the object which will be passed to functions and commands, if specified.

The `destructor` field is optional. If specified, it should have the following signature:
```java
public static void method(MinecraftServer server, S prevInstance);
```
This method will be called after reloads and before server shutdown. The parameter is always the same as the `prevInstance`
parameter in the factory method. That is: the value returned the last time the factory method was called. On reload, 
destructors are called after factories (if there is a previous instance available.)

State objects can also be accessed from other methods by using the DataBridge API:
```java
MinecraftServer server = [ ... ];
Optional<S> obj = ServerStateObjects.getStateObject(server, S.class, "namespace:path");

RegistryAccess access = [ ... ];
Optional<S> obj = ServerStateObjects.getStateObject(access, S.class, "namespace:path");
```

### Interaction Entities
Interaction entities can now directly execute functions, rather than just storing the last player who interacted with them.
To facilitate this, the mod adds two new NBT fields for interaction entities:
```nbtt
{
    functions: {
        "namespace:function_name": {param: "value"}
    },
    attack_functions: {
        "namespace:function_name": {param: "value"}
    }
}
```
The functions specified in the `functions` field will be called each time a player right-clicks the entity, as the player
who interacted.

The functions specified in the `attack_functions` field will be called each time a player left-clicks the entity, as the player
who attacked.

### Trigger Selector
The mod also adds a new entity selector type: `@t` This will target the entity which triggered the function. Meaning, 
for example, functions called from interaction entities will have `@s` target to the player who interacted, and `@t` 
target to the interaction entity itself.

The trigger entity can be accessed from a CommandSourceStack in Java via two different methods:

Using only Minecraft classes:
```java
EntitySelector triggerSelector = new EntitySelectorParser(new StringReader("@t"), true).parse(); // Cache this 
Entity trigger = triggerSelector.findSingleEntity(cs);
```

Using the DataBridge API:
```java
Entity trigger = ServerFunctionUtil.getTriggerEntity(cs);
```