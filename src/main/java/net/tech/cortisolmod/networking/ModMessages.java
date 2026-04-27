package net.tech.cortisolmod.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.tech.cortisolmod.CortisolMod;
import net.tech.cortisolmod.networking.packet.CortisolSyncS2CPacket;
import net.tech.cortisolmod.networking.packet.ExampleC2SPacket;
import net.tech.cortisolmod.networking.packet.StartIntroCinematicS2CPacket;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId=0;
    private static int id(){
        return packetId++;
    }
    public static void register(){
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(CortisolMod.MOD_ID,"messages"))
                .networkProtocolVersion(()->"1.0")
                .clientAcceptedVersions(s->true)
                .serverAcceptedVersions(s->true)
                .simpleChannel();
        INSTANCE=net;

        net.messageBuilder(ExampleC2SPacket.class,id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ExampleC2SPacket::new)
                .encoder(ExampleC2SPacket::toBytes)
                .consumerMainThread(ExampleC2SPacket::handle)
                .add();
        net.messageBuilder(CortisolSyncS2CPacket.class,id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(CortisolSyncS2CPacket::new)
                .encoder(CortisolSyncS2CPacket::toBytes)
                .consumerMainThread(CortisolSyncS2CPacket::handle)
                .add();
        net.messageBuilder(StartIntroCinematicS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(StartIntroCinematicS2CPacket::new)
                .encoder(StartIntroCinematicS2CPacket::toBytes)
                .consumerMainThread(StartIntroCinematicS2CPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message){
    INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(()->player), message);


    }
}
