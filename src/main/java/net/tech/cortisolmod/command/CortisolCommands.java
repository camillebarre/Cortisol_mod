package net.tech.cortisolmod.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tech.cortisolmod.CortisolMod;
import net.tech.cortisolmod.cortisol.PlayerCortisolProvider;
import net.tech.cortisolmod.networking.ModMessages;
import net.tech.cortisolmod.networking.packet.CortisolSyncS2CPacket;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.context.CommandContext;

import java.util.Collection;

@Mod.EventBusSubscriber(modid = CortisolMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CortisolCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {

        event.getDispatcher().register(
                Commands.literal("cortisol")
                        .requires(src -> src.hasPermission(2))

                        // ================= GET =================
                        .then(Commands.literal("get")

                                // /cortisol get (ALL PLAYERS)
                                .executes(ctx -> {
                                    var server = ctx.getSource().getServer();

                                    ctx.getSource().sendSuccess(() -> Component.literal("=== Cortisol joueurs ==="), false);

                                    for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                                        p.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(c -> {
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            p.getName().getString() + " : " + c.getCortisol()
                                                    ),
                                                    false
                                            );
                                        });
                                    }

                                    return 1;
                                })

                                // /cortisol get <player>
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");

                                            target.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(c -> {
                                                ctx.getSource().sendSuccess(
                                                        () -> Component.literal(
                                                                target.getName().getString() + " : " + c.getCortisol()
                                                        ),
                                                        false
                                                );
                                            });

                                            return 1;
                                        })
                                )
                        )

                        // ================= SET =================
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            return setCortisol(ctx, IntegerArgumentType.getInteger(ctx, "value"), null);
                                        })
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    return setCortisol(
                                                            ctx,
                                                            IntegerArgumentType.getInteger(ctx, "value"),
                                                            EntityArgument.getPlayer(ctx, "player")
                                                    );
                                                })
                                        )
                                )
                        )

                        // ================= ADD =================
                        .then(Commands.literal("add")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(ctx -> {
                                            return addCortisol(ctx, IntegerArgumentType.getInteger(ctx, "value"), null);
                                        })
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    return addCortisol(
                                                            ctx,
                                                            IntegerArgumentType.getInteger(ctx, "value"),
                                                            EntityArgument.getPlayer(ctx, "player")
                                                    );
                                                })
                                        )
                                )
                        )
        );
    }

    // ================= HELPERS =================

    private static int setCortisol(
            CommandContext<CommandSourceStack> ctx,
            float value,
            ServerPlayer target
    ) {
        try {
            ServerPlayer player = (target != null)
                    ? target
                    : ctx.getSource().getPlayerOrException();

            player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(c -> {
                c.setCortisol(value);

                ModMessages.sendToAllPlayers(
                        new CortisolSyncS2CPacket(player.getId(), c.getCortisol())
                );
            });

            ctx.getSource().sendSuccess(
                    () -> Component.literal("Cortisol défini sur " + player.getName().getString()),
                    true
            );

        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Erreur commande."));
        }

        return 1;
    }

    private static int addCortisol(
            CommandContext<CommandSourceStack> ctx,
            float value,
            ServerPlayer target
    ) {
        try {
            ServerPlayer player = (target != null)
                    ? target
                    : ctx.getSource().getPlayerOrException();

            player.getCapability(PlayerCortisolProvider.PLAYER_CORTISOL).ifPresent(c -> {
                c.addCortisol(value);

                ModMessages.sendToAllPlayers(
                        new CortisolSyncS2CPacket(player.getId(), c.getCortisol())
                );
            });

            ctx.getSource().sendSuccess(
                    () -> Component.literal("Cortisol modifié pour " + player.getName().getString()),
                    true
            );

        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Erreur commande."));
        }

        return 1;
    }
}