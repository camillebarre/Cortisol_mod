package net.tech.cortisolmod.event;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tech.cortisolmod.item.ModItems;
import net.tech.cortisolmod.networking.ModMessages;
import net.tech.cortisolmod.networking.packet.CortisolMobSyncS2CPacket;
import net.tech.cortisolmod.particle.ModParticles;
import net.tech.cortisolmod.util.AdvancementHelper;
import net.tech.cortisolmod.worldgen.biome.ModBiomes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;

import java.util.Random;
import java.util.Objects;
import java.util.UUID;

/*
Events related to special Cortisol Mob (potentially, all monsters)
 */
@Mod.EventBusSubscriber
public class CortisolMobEvents {
    private static final String TAG_CORTISOL = "cortisol_mob";

    private static final UUID SPEED_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID DAMAGE_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID HEALTH_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    // 0.5 to x1.5, 1 to x2, etx...
    private static final float SPEED_MODIFIER = 0.7f;
    private static final float DAMAGE_MODIFIER = 2.5f;
    private static final float HEALTH_MODIFIER = 2f;

    private static final double CHANCE = 0.05;
    private static final double WITHER_CHANCE = 0.2;

    // Special event for the wither
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof WitherBoss wither)) return;

        // Check if cortisol tag already exist
        CompoundTag tag = wither.getPersistentData();
        if (tag.getBoolean(TAG_CORTISOL)) return;

        if (wither.getRandom().nextDouble() > WITHER_CHANCE) {
            tag.putBoolean(TAG_CORTISOL, false);
            return;
        }
        Player closestPlayer = wither.level().getNearestPlayer(
                TargetingConditions.forNonCombat().range(50.0),
                wither
        );
        if (closestPlayer != null){
            AdvancementHelper.grant((ServerPlayer) closestPlayer, "cortisolmod:cortisol/gl_hf");
        }
        wither.setCustomName(
                Component.literal("Cortisol Wither")
                        .withStyle(ChatFormatting.RED)
        );
        applyCortisol(wither);
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        Mob mob = event.getEntity();

        // Server only, and only aggressive mobs
        if (mob.level().isClientSide) return;
        if (!(mob instanceof Monster)) return;

        // Check if cortisol tag already exist
        CompoundTag tag = mob.getPersistentData();
        if (tag.getBoolean(TAG_CORTISOL)) return;

        //spawn in cortisol biome
        if (event.getLevel().getBiome(mob.blockPosition()).is(ModBiomes.CORTISOL_BIOME)){
            applyCortisol(mob);
            return;
        }

        // RNG
        if (mob.getRandom().nextDouble() > CHANCE) {
            tag.putBoolean(TAG_CORTISOL, false);
            return;
        }
        applyCortisol(mob);
    }

    private static void applyCortisol(Mob mob) {
        CompoundTag tag = mob.getPersistentData();
        tag.putBoolean(TAG_CORTISOL, true);

        if (mob.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            Objects.requireNonNull(mob.getAttribute(Attributes.MOVEMENT_SPEED)).addPermanentModifier(
                    new AttributeModifier(SPEED_UUID, "cortisol_speed", SPEED_MODIFIER, AttributeModifier.Operation.MULTIPLY_TOTAL)
            );
        }
        if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            Objects.requireNonNull(mob.getAttribute(Attributes.ATTACK_DAMAGE)).addPermanentModifier(
                    new AttributeModifier(DAMAGE_UUID, "cortisol_damage", DAMAGE_MODIFIER, AttributeModifier.Operation.MULTIPLY_TOTAL)
            );
        }
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
            Objects.requireNonNull(mob.getAttribute(Attributes.MAX_HEALTH)).addPermanentModifier(
                    new AttributeModifier(HEALTH_UUID, "cortisol_health", HEALTH_MODIFIER, AttributeModifier.Operation.MULTIPLY_TOTAL)
            );
        }

        // Heal after health changes
        mob.setHealth(mob.getMaxHealth());

        // Simple visuals
        //mob.setCustomNameVisible(true);
        // Uncomment to set a custom name to the mob ("Cortisol [mobname]" in red)
        // mob.setCustomName(net.minecraft.network.chat.Component.literal("§cCortisol " + mob.getName().getString()));


        mob.getPersistentData().putBoolean("cortisol_mob", true);
        ModMessages.sendToAllPlayers(
                new CortisolMobSyncS2CPacket(
                        mob.getId(),
                        true
                )
        );
        }

    // Made special drop for cortisol mob
    @SubscribeEvent
    public static void onMobDrops(LivingDropsEvent event) {
        Random random = new Random();
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide) return;
        if (!entity.getPersistentData().getBoolean(TAG_CORTISOL)) return;

        Level level = entity.level();

        // Drop cortilium (random between 1 and 2)

        ItemStack cortilium = new ItemStack(ModItems.CORTILIUM.get(), random.nextInt(2)+1);

        ItemEntity drop = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), cortilium);
        event.getDrops().add(drop);

        // Advancement
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            AdvancementHelper.grant(player, "cortisolmod:kill_cortisol_mob");
        }
    }




    // Set particles effects on these mobs
    @SubscribeEvent
    public static void onLivingTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        var entity = event.getEntity();

        if (entity.level().isClientSide) return;
        if (!entity.getPersistentData().getBoolean(TAG_CORTISOL)) return;

        if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel level)) return;

        // optimisation : pas chaque tick
        if (entity.tickCount % 5 != 0) return;

        spawnEliteAura(level, entity);
    }

    private static void spawnEliteAura(net.minecraft.server.level.ServerLevel level,
                                       net.minecraft.world.entity.LivingEntity entity) {

        double centerX = entity.getX();
        double centerZ = entity.getZ();

        double baseY = entity.getY();
        double height = entity.getBbHeight();

        float radius = 1f;

        double x = centerX + (entity.getRandom().nextDouble() - 0.5) * radius;
        double y = baseY + entity.getRandom().nextDouble() * height;
        double z = centerZ + (entity.getRandom().nextDouble() - 0.5) * radius;

        level.sendParticles(
                ModParticles.CORTISOL_PARTICLE.get(),
                x, y, z,
                1,
                0, 0, 0,
                0f
        );
    }
}