package net.tech.cortisolmod.client.cinematic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class MusicBlocker {

    private static boolean customMusicPlaying = false;
    private static SoundInstance currentMusicInstance = null;

    public static final SoundEvent CUSTOM_MUSIC = SoundEvents.MUSIC_DISC_OTHERSIDE;

    public static void playCustomMusic() {
        Minecraft mc = Minecraft.getInstance();

        mc.execute(() -> {
            mc.getSoundManager().stop(null, SoundSource.MUSIC);

            currentMusicInstance = SimpleSoundInstance.forMusic(CUSTOM_MUSIC);
            customMusicPlaying = true;

            mc.getSoundManager().play(currentMusicInstance);
        });
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!customMusicPlaying) return;

        Minecraft mc = Minecraft.getInstance();

        if (currentMusicInstance == null) {
            customMusicPlaying = false;
            return;
        }

        if (!mc.getSoundManager().isActive(currentMusicInstance)) {
            customMusicPlaying = false;
            currentMusicInstance = null;
        }
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (!customMusicPlaying) return;
        if (event.getSound() == null) return;

        if (event.getSound().getSource() == SoundSource.MUSIC) {

            // Autorise only the custom music
            if (currentMusicInstance != null && event.getSound() == currentMusicInstance) {
                return;
            }

            event.setSound(null);
        }
    }
}