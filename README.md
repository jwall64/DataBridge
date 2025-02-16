## DataBridge

An attempt to bridge the gap between data packs and mods.

### Java Functions
With this mod installed, data packs can register functions that point to Java code. To do this, create a `.json` file
in the pack's `function` folder with the following format:
```json
{
  "type": "",
  "value": ""
}
```
The `type` field can be either `method` or `object`. 

If the type is `method`, `value` should be a method reference in the
form `<fully.qualified.class.Name>::<method>`. The method with that name should have the following signature:
```java
public static void method(CommandSourceStack css,
            CompoundTag tag,
            ResourceLocation id,
            CommandDispatcher<CommandSourceStack> dispatcher,
            ExecutionContext<CommandSourceStack> ctx,
            Frame frame) { }
```

If the type is `object`, `value` should be the fully qualified class name of a class which implements
`CommandFunction<CommandSourceStack>`. The class should have a public constructor with the following signature:
```java
MyFunction(ResourceLocation id);
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
  "permission_node": null
}
```
The `name` field is the name of the command. 
The `permission_level` field is optional, and defaults to 0. This is the operator level a player needs to run the command.
The `permission_node` field is optional. If specified, players will need that permission node (or the specified permission level)
to execute the command.

The `type` field can be `alias`, `method`, or `builder`.

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
public static int method(CommandContext<CommandSourceStack> context) { }
```

If the type is `builder`, `value` should be a method reference in the
form `<fully.qualified.class.Name>::<method>`. The method with that name should have the following signature:
```java
public static LiteralArgumentBuilder<CommandSourceStack> method(String id, LiteralArgumentBuilder<CommandSourceStack> builder) { }
```

