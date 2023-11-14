# Tunnel

Tunnel is a maven dependency that provide certain tools to help module-managed multi-version development on Minecraft mods.

## Install

To use this in your mod project (like mod-loader-1.x.x), you need to create a module (like mod-common) that hold your standalone mod-logic.

- mod-loader-1.x.x need to have access to mod-common
- both of the modules require access to *tunnel*
- mod-loader-1.x.x need to make sure that the yield need to include both mod-common and *tunnel*

## Usage

The essence of *tunnel* is to offer a way for mod-common have access to Minecraft class (any class, in fact) by sealing or generating an instance.

And all the mod-loader-1.x.x need to do is to achieve some necessary method (like mod initiator, mixin, etc.).

Then leave all code that builds up your mod to the mod-common.

The bridge that connects mod-common with Minecraft is to seal everything you need by creating `TunnelClass`! And don't forget to add the implementation of your `TunnelClass` to `tunnel.json`.

``` json
{
    "tunnels": [
        "xxx.xxx.McExampleImpl"
    ]
}
```

You always need to initiate your mod with tunnel via `TunnelInitiator.init(YourModMainClass.class);`

### 1. Access constructor

Assuming you are trying to instantiate `TranslatableTextContent`.

1. Create an abstract class `McTranslatableTextContent` in mod-common with codes below.

``` java
public abstract class McTranslatableTextContent implements McTunnel {
    // Important!!! This is the Tunnel Instance to create that provides "constructor"
    public static McTranslatableTextContent MC_TRANSLATABLE_TEXT_CONTENT;
    
    abstract McTranslatableTextContent of(String key, @Nullable String fallback, Object[] args);
}
```

2. Create a class `McTranslatableTextContentImpl` in mod-loader-1.x.x with codes below.

``` java
@Tunnel
public class McTranslatableTextContentImpl extends McBlockHitResult {
    private TranslatableTextContent translatableTextContent;
    
    // Seal instance
    public static McTranslatableTextContentImpl of(TranslatableTextContent translatableTextContent) {
        McTranslatableTextContentImpl mcTranslatableTextContent = new McTranslatableTextContentImpl();
        mcTranslatableTextContent.translatableTextContent = translatableTextContent
        return mcTranslatableTextContent;
    }
    
    // This is your constructor to use
    @Override
    public McTranslatableTextContent of(String key, @Nullable String fallback, Object[] args) {
        TranslatableTextContent translatableTextContent = new McTranslatableTextContent(key, fallback, args);
        return McTranslatableTextContentImpl.of(translatableTextContent);
    }
    
    // Important!!! Initiate your Tunnel instance
    @Override
    public void initTunnel() {
        MC_TRANSLATABLE_TEXT_CONTENT = new McTranslatableTextContentImpl();
    }
    ...
}
```

3. add your tunnel-implementation path to "tunnels.json" in resource directory of mod-loader-1.x.x.

``` json
{
    "tunnels": [
        "xxx.xxx.McTranslatableTextContentImpl"
    ]
}
```

4. Then in your mod-main-class, add a sentence to initiate your mod with *Tunnel*

``` java
public class ExampleMod implements ModInitializer {
    @Override
    public void init() {
        // Initiate your mod with Tunnel
        TunnelInitiator.init(ExampleMod.class);
    }
}
```

And that's all the pre-handle! Then you can invoke the "constructor" in mod-common like codes below.

``` java
import xxx.McTranslatableTextContent.MC_TRANSLATABLE_TEXT_CONTENT

...

public static void func(String key, @Nullable String fallback, Object[] args) {
    McTranslatableTextContent content = MC_TRANSLATABLE_TEXT_CONTENT.of(key, fallback, args);
}
```

### 2. Access static fields

Following *#1. Access constructor*, taking another example minecraft class `Hand`

1. Create abstract class `McHand` in mod-common like above, with static fields `Hand.UP` you want to access.

``` java 
public abstract class McHand implements McTunnel {
    public static McHand MAIN_HAND;
}
```

2. just like *#1*, create tunnel-implementation `McHandImpl`, but differently on `initTunnel()`

``` java
@Tunnel
public class McHandImpl extends McHand {
    private Hand hand;

    public static McHandImpl of(Hand hand) {
        McHandImpl mcHand = new McHandImpl();
        mcHand.hand = hand;
        return mcHand;
    }
    
    @Override
    public void initTunnel() {
        UP = of(Hand.MAIN_HAND);
    }
...
}
```

3. Add your tunnel-implementation path to "tunnels.json" in resource directory of mod-loader-1.x.x.

``` json
{
    "tunnels": [
        "xxx.xxx.McHandImpl"
    ]
}
```

4. Then in your mod-main-class, add a sentence to initiate your mod with *Tunnel*

``` java
public class ExampleMod implements ModInitializer {
    @Override
    public void init() {
        // Initiate your mod with Tunnel
        TunnelInitiator.init(ExampleMod.class);
    }
}
```

That's all! And `McHand.MAIN_HAND` is `Hand.MAIN_HAND`.

### 3. Access methods or member-variables

Assuming you are trying to invoke `TranslatableTextContent#getArg(int index)`

1. Create abstract method in *TunnelClass* of mod-common

``` java 
public abstract class McTranslatableTextContent implements McTunnel {
...
    abstract StringVisitable getArg(int index);
...
}
```

2. Implement abstract method in *TunnelImplementation* of mod-loader-1.x.x

``` java 
@Tunnel
public class McTranslatableTextContentImpl extends McTranslatableTextContent {
...
    @Override
    public final StringVisitable getArg(int index) {
        return this.translatableTextContent.getArg(index);
    }
...
}
```

#### 3.1 What if the method requires args that also cannot be accessed in mod-common?

First, all the class you cannot access directly must be created corresponding tunnel classes!

For example, you want to invoke `ItemStack#useOnBlock(ItemUsageContext)`

1. Create tunnel classes `McItemStack`, `McItemUsageContext` in mod-common and their implementation `McItemStackImpl`, `McItemUsageContextImpl`.

2. Create method `McItemStack#useOnBlock(McItemUsageContext)`

``` java
public abstract class McItemStack implements McTunnel {
...
    abstract void useOnblock(McItemUsageContext context);
...
}
```

3. Implement getter `McItemUsageContextImpl#get()`

``` java
@Tunnel
public class McItemUsageContextImpl extends McItemUsageContext {
...
    private ItemUsageContext itemUsageContext;
    
    @Override
    public Object get() {
        return this.itemUsageContext;
    }
...
}
```

4. Implement `McItemStackImpl#useOnBlock(McItemUsageContext)`

``` java
@Tunnel
public class McItemStackImpl extends McItemStack {
...
    private ItemStack itemStack;
    
    @Override
    public void useOnBlock(McItemUsageContext context) {
        ItemUsageContext context = (ItemUsageContext) context.get();
        this.itemStack.useOnBlock(context);
    }
...
}
```