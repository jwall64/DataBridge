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
            Identifier id,
            CommandDispatcher<CommandSourceStack> dispatcher,
            ExecutionContext<CommandSourceStack> ctx,
            Frame frame,
            S state) { }
```
If no state object is specified, The state object type is Void.

If the type is `object`, `value` should be method reference in the
form `<fully.qualified.class.Name>::<method>`. The method with that name should have the following signature:
```java
public static CommandFunction<CommandSourceStack> method(Identifier id, Supplier<S> state);
```

### Command Definitions
This mod also allows data packs to define commands. Commands can either be aliases to existing commands, or pointers
to Java code. Commands are registered in the `command` folder in data packs. Commands are `.json` files with the following format:
```json
{
  "name": "mycommand",
  "type": "",
  "value": "",
  "permission_level": "all",
  "permission_node": null,
  "state_object": null
}
```
The `name` field is the name of the command.<br/>
The `type` field can be `alias`, `method`, or `builder`.<br/>
The `permission_level` field is optional, and defaults to "all". This is the [permission level](https://minecraft.wiki/w/Permission_level) a player needs to run the command.<br/>
The `permission_node` field is optional. If specified, players will need that permission node (or the above permission level) to execute the command. (A permission management system such as [LuckPerms](https://luckperms.net/) is required to give players permission nodes.)<br/>
The `state_object` field is optional. If present, it is the name of a state object. (See below)<br/>

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
public static S method(MinecraftServer server, @Nullable S prevInstance);
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

### Extra Function Tags
DataBridge adds a few extra function tags for data packs to use:
- `databridge:join`: Functions with this tag are called when a player joins the server, right before the join message is sent. The command source for these functions is the player who is joining.
- `databridge:joined`: Functions with this tag are called when a player has fully joined the server, after the join message is sent. The command source for these functions is the player who joined.
- `databridge:leave`: Functions with this tag are called when a player leaves the server, before the leave message is sent and before the player data is saved. The command source for these functions is the player who left.

For all three of these functions, the player's username and uuid are passed to the function in the `with` parameter in the following format:
```json
{ 
    "username": "playername", 
    "uuid": "playeruuid" 
}
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

### Custom Click Event Handlers
Data packs can also define handlers for click events with the [`custom`](https://minecraft.wiki/w/Text_component_format#custom) action type (introduced in Minecraft 1.21.6). 
To do this, create a json file in the `click_event_handler` folder with the following format:
```json
{
  "function": "namespace:function_name"
}
```
The `function` field should be a reference to a registered function in the form `namespace:function_name`. The function will receive the `payload` field of the click event as its `with` parameter. The command source will be the player who clicked. 

For example, if the click event is defined as follows:
```json
{
  "action": "custom",
  "id": "mypack:my_handler",
  "payload": {"message": "Hello, world!"}
}
```
And the click event handler function is defined as follows:
```json
{
    "function": "mypack:handle_click"
}
```
Then when a player clicks the text with that click event, the `handle_click` function will be executed with the command source of the player and a `with` parameter of `{"message": "Hello, world!"}`.

### Functional Item Components
With DataBridge installed, item stacks can be configured to execute functions when used. To do this, add the following NBT data to the item's `minecraft:custom_data` component:
```nbtt
{
    "databridge:functional": {
        "<action>": [ 
            { 
                "id": "namespace:function_name", 
                "with": { "param": "value"} 
            }
        ]
    }
}
```
where `<action>` is the item use action that triggers the function. Possible values for `<action>` are:
- `attack`: Triggered when the item is used to attack an entity.
- `piercing_attack`: Triggered when the item is used to perform a piercing attack, regardless of whether an entity is actually hit. (This can be used for left-click detection, but it requires the item to have the `minecraft:piercing_weapon` component.)
- `interact`: Triggered whenever the item is right-clicked, regardless of what it is clicked on.
- `interact_block`: Triggered when the item is right-clicked on a block.
- `interact_entity`: Triggered when the item is right-clicked on an entity.
- `break_block`: Triggered when the item is used to break a block.

When the item is used with the specified action, the corresponding functions will be executed with the user as the command source and the specified `with` parameter.

### Timeline Events
With DataBridge, data packs can also register functions to be executed at specific points on a vanilla [timeline](https://minecraft.wiki/w/Timeline) (introduced in 1.21.11.) This is done through a new field in the timeline JSON format. To add a timeline event, add the following field to a timeline JSON file: 
```json
{
    "events": [
        {
            "tick": 0,
            "function": "namespace:function_name",
            "with": {"param": "value"}
        }
    ]
}
```

A minimal timeline JSON file that executes a function every 100 ticks (5 seconds) might look something like this:
```json
{
    "period_ticks": 100,
    "clock": "minecraft:overworld",
    "tracks": {},
    "time_markers": {},
    "events": [
        {
            "tick": 0,
            "function": "namespace:function_name",
            "with": {}
        }
    ]
}
```
