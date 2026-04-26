# ArsMagica2 Network Package

This package contains the modernized networking infrastructure for ArsMagica2.

## Structure

```
am2.network/
├── AMPacket.java           - Base class for all packets
├── AMNetworkHandler.java   - Centralized packet registration
└── packets/                - Individual packet implementations
    ├── PacketTKDistanceSync.java
    ├── PacketAbilityToggle.java
    ├── PacketVelocityAdd.java
    └── PacketPlayerLogin.java
```

## Quick Start

### Creating a New Packet

1. **Create your packet class** in `am2.network.packets`:

```java
package am2.network.packets;

import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketYourName extends AMPacket<PacketYourName> {
    
    // Your data fields
    private int data;
    
    // Required empty constructor
    public PacketYourName() {}
    
    // Constructor with parameters
    public PacketYourName(int data) {
        this.data = data;
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(data);
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        data = buf.readInt();
    }
    
    @Override
    protected void handleServerSide(PacketYourName message, MessageContext ctx) {
        // Handle on server
    }
    
    // OR for client-bound packets:
    @Override
    protected void handleClientSide(PacketYourName message, MessageContext ctx) {
        // Handle on client
    }
}
```

2. **Register your packet** in `AMNetworkHandler.init()`:

```java
registerPacket(PacketYourName.class, PacketYourName.class, Side.SERVER);
```

3. **Send your packet**:

```java
// To server
AMNetworkHandler.getNetwork().sendToServer(new PacketYourName(42));

// To player
AMNetworkHandler.getNetwork().sendTo(new PacketYourName(42), player);

// To all near point
import net.minecraftforge.fml.common.network.NetworkRegistry;

AMNetworkHandler.getNetwork().sendToAllAround(
    new PacketYourName(42),
    new NetworkRegistry.TargetPoint(dim, x, y, z, radius)
);
```

## Documentation

- See `PACKET_MIGRATION_GUIDE.md` for detailed migration instructions
- See `PACKET_ARCHITECTURE_ANALYSIS.md` for architecture details
- See example packets in this package for reference implementations

## Examples

- **Simple server packet**: `PacketTKDistanceSync`
- **String data**: `PacketAbilityToggle`
- **Client packet**: `PacketVelocityAdd`
- **Binary data**: `PacketPlayerLogin`

## Migration Status

This is the **new** packet system. The old system (AMNetHandler, AMTileEntityNetHandler) is being phased out.

When adding new packets, **always use this system**.

For migrating old packets, see the migration guide.
