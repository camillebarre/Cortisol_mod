package net.tech.cortisolmod.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tech.cortisolmod.CortisolMod;
import net.tech.cortisolmod.cortisol.PlayerCortisolProvider;
import net.tech.cortisolmod.networking.ModMessages;
import net.tech.cortisolmod.networking.packet.CortisolSyncS2CPacket;

@Mod.EventBusSubscriber(modid = CortisolMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CortisolCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("cortisol")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("add")
                    .then(Commands.argument("value", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            int value = IntegerArgumentType.getInteger(ctx, "value");

                            player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                                cortisol.setCortisol(cortisol.getCortisol() + value);
                                ModMessages.sendToPlayer(
                                    new CortisolSyncS2CPacket(cortisol.getCortisol()),
                                    player
                                );
                            });

                            ctx.getSource().sendSuccess(() -> Component.literal("Cortisol mis à jour."), true);
                            return 1;
                        })
                    )
                )

                .then(Commands.literal("set")
                    .then(Commands.argument("value", IntegerArgumentType.integer())
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            int value = IntegerArgumentType.getInteger(ctx, "value");

                            player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(cortisol -> {
                                cortisol.setCortisol(value);
                                ModMessages.sendToPlayer(
                                    new CortisolSyncS2CPacket(cortisol.getCortisol()),
                                    player
                                );
                            });

                            ctx.getSource().sendSuccess(() -> Component.literal("Cortisol défini."), true);
                            return 1;
                        })
                    )
                )
        );
    }
}
