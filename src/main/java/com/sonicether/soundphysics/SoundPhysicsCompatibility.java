package com.sonicether.soundphysics;

import paulscode.sound.SoundSystemConfig;

public final class SoundPhysicsCompatibility {

    private SoundPhysicsCompatibility() {}

    public static int selectAttenuationModel(int requestedModel, int configuredModel) {
        // NONE is intentional for ambient/UI audio. MAtmos may place these sources far away (for example, Y + 2048).
        return requestedModel == SoundSystemConfig.ATTENUATION_NONE ? requestedModel : configuredModel;
    }

    public static float selectDistanceOrRolloff(int requestedModel, float requestedValue, float configuredRolloff) {
        return requestedModel == SoundSystemConfig.ATTENUATION_NONE ? requestedValue : configuredRolloff;
    }

    public static boolean shouldEvaluateEnvironment(int attenuationModel) {
        return attenuationModel != SoundSystemConfig.ATTENUATION_NONE;
    }
}
