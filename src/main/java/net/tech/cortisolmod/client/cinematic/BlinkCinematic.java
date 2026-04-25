package net.tech.cortisolmod.client.cinematic;

/**
 * Gère l'animation de clignement.
 *
 * L'œil peut être demandé à s'ouvrir ou se fermer à n'importe quel pourcentage.
 * Appuyer sur B alterne entre fermé (100%) et ouvert (0%).
 *
 * État interne :
 *   currentAmount  : valeur courante du clignement  (0.0 = ouvert, 1.0 = fermé)
 *   targetAmount   : valeur cible
 *   eyeClosed      : true si l'état logique courant est "fermé"
 */
public class BlinkCinematic {

    // Durée de la transition en ms (par defaut)
    private static final long TRANSITION_DURATION = 300;

    // Valeur courante interpolée (0.0 = ouvert, 1.0 = fermé)
    private static float currentAmount = 0f;

    // Valeur cible
    private static float targetAmount = 0f;

    // Timestamp du début de la transition
    private static long transitionStart = 0;

    // Valeur de départ de la transition
    private static float fromAmount = 0f;

    // État logique actuel (false = ouvert, true = fermé)
    private static boolean eyeClosed = false;

    /**
     * Appelé par le KeyInputHandler quand B est pressé.
     * Alterne entre fermer et ouvrir l'œil.
     */
    public static void toggle() {
        eyeClosed = !eyeClosed;
        animateTo(eyeClosed ? 1.0f : 0.0f);
    }

    /**
     * Lance une animation vers un pourcentage précis.
     *
     * @param target valeur cible entre 0.0 (ouvert) et 1.0 (fermé)
     */
    public static void animateTo(float target, long durationMs) {
        target = Math.max(0f, Math.min(1f, target));
        fromAmount = currentAmount;
        targetAmount = target;
        transitionStart = System.currentTimeMillis();
        currentTransitionDuration = durationMs;
    }

    public static void animateTo(float target) {
        animateTo(target, TRANSITION_DURATION);
    }

    /**
     * @return true si l'écran n'est pas complètement transparent (currentAmount > 0), ou si logo du jeu visible.
     */
    public static boolean isPlaying() {
        // On lit currentAmount directement, sans recalculer
        return currentAmount > 0f || logoVisible;
    }

    /**
     * Retourne la valeur interpolée courante.
     */
    public static float getBlinkAmount() {
        // Unique point de mise à jour par frame
        updateCurrentAmount();
        return currentAmount;
    }

    private static long currentTransitionDuration = TRANSITION_DURATION;

    private static void updateCurrentAmount() {
        tickSequence();

        if (targetAmount == fromAmount) {
            currentAmount = targetAmount;
            return;
        }

        long elapsed = System.currentTimeMillis() - transitionStart;
        float t = elapsed / (float) currentTransitionDuration;

        if (t >= 1f) {
            currentAmount = targetAmount;
            fromAmount = targetAmount;
            return;
        }

        t = t * t * (3f - 2f * t);
        currentAmount = fromAmount + (targetAmount - fromAmount) * t;
    }


    // File d'étapes : {targetAmount, délai avant de démarrer cette étape en ms}
    private static final java.util.Queue<long[]> sequenceQueue = new java.util.ArrayDeque<>();

    /**
     * Format : target0, duration0, pause0, target1, duration1, pause1, ...
     * duration : temps en ms pour atteindre la cible (-1 = utilise TRANSITION_DURATION par défaut)
     * pause    : temps d'attente après la fin de la transition avant la prochaine étape
     */
    public static void playSequence(float... steps) {
        sequenceQueue.clear();
        long now = System.currentTimeMillis();
        long cursor = now;
        int i = 0;
        while (i < steps.length) {
            float target   = steps[i];
            long  duration = (i + 1 < steps.length) ? (long) steps[i + 1] : TRANSITION_DURATION;
            long  pause    = (i + 2 < steps.length) ? (long) steps[i + 2] : 0L;
            if (duration < 0) duration = TRANSITION_DURATION;
            // on stocke : {bits(target), timestamp de déclenchement, durée}
            sequenceQueue.add(new long[]{Float.floatToRawIntBits(target), cursor, duration});
            cursor += duration + pause;
            i += 3;
        }
        tickSequence();
    }

    private static void tickSequence() {
        if (sequenceQueue.isEmpty()) return;
        long now = System.currentTimeMillis();
        long[] next = sequenceQueue.peek();
        if (now >= next[1]) {
            sequenceQueue.poll();
            float target  = Float.intBitsToFloat((int) next[0]);
            long  duration = next[2];
            animateTo(target, duration);
            tickSequence();
        }
    }



    // Logo
    private static boolean logoVisible = false;
    private static long logoFadeInStartTime = 0;
    private static long logoFadeOutStartTime = 0;
    private static final long LOGO_FADE_DURATION = 800;
    private static boolean logoFadingIn = false;
    private static boolean logoFadingOut = false;

    public static void showLogo() {
        logoVisible = true;
        logoFadingIn = true;
        logoFadingOut = false;
        logoFadeInStartTime = System.currentTimeMillis();
    }

    public static void startLogoFadeOut() {
        if (!logoVisible) return;
        logoFadingIn = false;
        logoFadingOut = true;
        logoFadeOutStartTime = System.currentTimeMillis();
    }

    public static boolean isLogoVisible() {
        return logoVisible;
    }

    public static float getLogoAlpha() {
        if (!logoVisible) return 0f;

        if (logoFadingIn) {
            long elapsed = System.currentTimeMillis() - logoFadeInStartTime;
            float t = elapsed / (float) LOGO_FADE_DURATION;
            if (t >= 1f) {
                logoFadingIn = false;
                return 1f;
            }
            return t * t * (3f - 2f * t); // smooth-step
        }

        if (logoFadingOut) {
            long elapsed = System.currentTimeMillis() - logoFadeOutStartTime;
            float t = elapsed / (float) LOGO_FADE_DURATION;
            if (t >= 1f) {
                logoVisible = false;
                return 0f;
            }
            float ft = t * t * (3f - 2f * t);
            return 1f - ft; // smooth-step inversé
        }

        return 1f; // pleinement visible entre les deux fades
    }
}
