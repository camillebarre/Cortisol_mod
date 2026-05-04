package net.tech.cortisolmod.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tech.cortisolmod.client.ClientCortisolData;

import java.util.function.Supplier;

public class CortisolSyncS2CPacket {

    private final int entityId;
    private final float cortisol;

    public CortisolSyncS2CPacket(int entityId, float cortisol) {
        this.entityId = entityId;
        this.cortisol = cortisol;
    }

    public CortisolSyncS2CPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.cortisol = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeFloat(cortisol);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ClientCortisolData.set(entityId, cortisol);
        });

        return true;
    }
}