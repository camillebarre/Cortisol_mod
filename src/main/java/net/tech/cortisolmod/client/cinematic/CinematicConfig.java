package net.tech.cortisolmod.client.cinematic;

import com.google.gson.*;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.tech.cortisolmod.CortisolMod;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CinematicConfig {

    public record SequenceStep(float target, long duration, long pause) {}

    public record LogoConfig(long appearMs, long fadeOutMs) {}

    private static java.util.List<SequenceStep> sequence = new java.util.ArrayList<>();
    private static LogoConfig logo = new LogoConfig(3000, 4500);

    public static void load(ResourceManager manager) {
        ResourceLocation loc = new ResourceLocation(CortisolMod.MOD_ID, "cinematic/intro.json");
        try (InputStream is = manager.getResource(loc).get().open();
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            // Lecture séquence
            sequence.clear();
            for (JsonElement el : root.getAsJsonArray("sequence")) {
                JsonObject step = el.getAsJsonObject();
                float target  = step.get("target").getAsFloat();
                long duration = step.get("duration").getAsLong();
                long pause    = step.get("pause").getAsLong();
                sequence.add(new SequenceStep(target, duration, pause));
            }

            // Lecture logo
            JsonObject logoObj = root.getAsJsonObject("logo");
            long appearMs  = logoObj.get("appear_ms").getAsLong();
            long fadeOutMs = logoObj.get("fade_out_ms").getAsLong();
            logo = new LogoConfig(appearMs, fadeOutMs);

        } catch (Exception e) {
            System.err.println("[testscene] Erreur lecture intro.json : " + e.getMessage());
        }
    }

    public static float[] buildSequenceArray() {
        float[] arr = new float[sequence.size() * 3];
        for (int i = 0; i < sequence.size(); i++) {
            SequenceStep s = sequence.get(i);
            arr[i * 3]     = s.target();
            arr[i * 3 + 1] = s.duration();
            arr[i * 3 + 2] = s.pause();
        }
        return arr;
    }

    public static LogoConfig getLogo() { return logo; }
}