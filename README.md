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
            <State object type> state) { }
```
If no state object is specified, The state object type is Void.

If the type is `object`, `value` should be the fully qualified class name of a class which implements
`CommandFunction<CommandSourceStack>`. The class should have a public constructor with the following signature:
```java
MyFunction(ResourceLocation id, <State object type> state);
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
public static int method(CommandContext<CommandSourceStack> context, <State object type> state) { }
```

If the type is `builder`, `value` should be a method reference in the
form `<fully.qualified.class.Name>::<method>`. The method with that name should have the following signature:
```java
public static LiteralArgumentBuilder<CommandSourceStack> method(String id, LiteralArgumentBuilder<CommandSourceStack> builder, <State object type> state) { }
```

### State Objects
For sharing state between functions/commands, data packs can define state objects. To do so, put a json file in the `state_object`
folder with the following format:
```json
{
  "class": "fully.qualified.class.Name"
}
```
This should be an object with a public, default constructor. It will be created when data packs are loaded for the first time.
This is the object which will be passed to functions and commands, if specified.