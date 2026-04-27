package net.tech.cortisolmod.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tech.cortisolmod.client.cinematic.BlinkCinematic;

import java.util.function.Supplier;

public class StartIntroCinematicS2CPacket {

    public StartIntroCinematicS2CPacket() {
    }

    public StartIntroCinematicS2CPacket(FriendlyByteBuf buf) {
        // rien à lire
    }

    public void toBytes(FriendlyByteBuf buf) {
        // rien à écrire
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(BlinkCinematic::playCinematic);
    }
}