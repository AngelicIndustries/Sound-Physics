package com.sonicether.soundphysics;

import net.minecraft.client.audio.SoundCategory;

import paulscode.sound.SoundSystemConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SoundPhysicsCompatibility {

    private SoundPhysicsCompatibility() {}

    public static int selectAttenuationModel(int requestedModel, int configuredModel) {
        // NONE is intentional for ambient/UI audio. MAtmos may place these sources far away (for example, Y + 2048).
        return requestedModel == SoundSystemConfig.ATTENUATION_NONE ? requestedModel : configuredModel;
    }

    public static float selectDistanceOrRolloff(int requestedModel, float requestedValue, float configuredRolloff) {
        return requestedModel == SoundSystemConfig.ATTENUATION_NONE ? requestedValue : configuredRolloff;
    }

    /** The range each new source asked for, until it plays (sources that never play are let go past the cap). */
    private static final Map<String, Float> REQUESTED_RANGES = new ConcurrentHashMap<>();
    private static final int MAX_PENDING_RANGES = 512;
    /** The range of a sound at volume 1, which keeps OpenAL's reference distance of one block. */
    private static final float VANILLA_RANGE = 16.0F;

    /**
     * Keeps the range a linear source asked for: 16 blocks per unit of volume above 1, or another mod's (AngelLib's
     * custom distance). The configured rolloff takes the place of the range, so the range sets the reference distance.
     */
    public static void rememberRange(String sourcename, int requestedModel, float requestedValue) {
        if (sourcename == null || requestedModel != SoundSystemConfig.ATTENUATION_LINEAR || requestedValue <= 0) return;
        if (REQUESTED_RANGES.size() > MAX_PENDING_RANGES) REQUESTED_RANGES.clear();
        REQUESTED_RANGES.put(sourcename, requestedValue);
    }

    /**
     * OpenAL's reference distance for a source, up to which it plays at full volume before the rolloff: a sixteenth
     * of its range, so a vanilla sound keeps one block and louder or farther-reaching ones carry as far as they meant to.
     */
    public static float takeReferenceDistance(String sourcename) {
        Float range = sourcename == null ? null : REQUESTED_RANGES.remove(sourcename);
        return range == null ? 1.0F : Math.max(1.0F, range / VANILLA_RANGE);
    }

    /** A queued sound's category and name, by source name. */
    public static final class PendingSound {
        public final SoundCategory category;
        public final String name;

        PendingSound(SoundCategory category, String name) {
            this.category = category;
            this.name = name;
        }
    }

    private static final Map<String, PendingSound> PENDING_SOUNDS = new ConcurrentHashMap<>();

    /**
     * Remembers what a sound is before it is queued. The sound thread plays it later, by which time other sounds may
     * have been queued, so a single "last sound" would give it their category (an interface sound's, say).
     */
    public static void rememberSound(String sourcename, SoundCategory category, String name) {
        if (sourcename == null) return;
        if (PENDING_SOUNDS.size() > MAX_PENDING_RANGES) PENDING_SOUNDS.clear();
        PENDING_SOUNDS.put(sourcename, new PendingSound(category, name));
    }

    public static PendingSound takeSound(String sourcename) {
        return sourcename == null ? null : PENDING_SOUNDS.remove(sourcename);
    }

    public static boolean shouldEvaluateEnvironment(int attenuationModel) {
        return attenuationModel != SoundSystemConfig.ATTENUATION_NONE;
    }
}
