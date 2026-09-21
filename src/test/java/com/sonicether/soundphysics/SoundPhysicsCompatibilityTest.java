package com.sonicether.soundphysics;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sonicether.soundphysics.mixins.early.MixinSoundSystem;

import paulscode.sound.CommandObject;
import paulscode.sound.CommandThread;
import paulscode.sound.SoundSystemConfig;

import java.net.MalformedURLException;
import java.net.URL;

public class SoundPhysicsCompatibilityTest {

    @Test
    public void preservesNonAttenuatedSources() {
        assertEquals(
                SoundSystemConfig.ATTENUATION_NONE,
                SoundPhysicsCompatibility.selectAttenuationModel(
                        SoundSystemConfig.ATTENUATION_NONE, SoundSystemConfig.ATTENUATION_ROLLOFF));
        assertEquals(
                16.0f,
                SoundPhysicsCompatibility.selectDistanceOrRolloff(
                        SoundSystemConfig.ATTENUATION_NONE, 16.0f, 1.0f),
                0.0f);
        assertFalse(SoundPhysicsCompatibility.shouldEvaluateEnvironment(SoundSystemConfig.ATTENUATION_NONE));
    }

    @Test
    public void appliesConfiguredPhysicsToAttenuatedSources() {
        assertEquals(
                SoundSystemConfig.ATTENUATION_ROLLOFF,
                SoundPhysicsCompatibility.selectAttenuationModel(
                        SoundSystemConfig.ATTENUATION_LINEAR, SoundSystemConfig.ATTENUATION_ROLLOFF));
        assertEquals(
                1.25f,
                SoundPhysicsCompatibility.selectDistanceOrRolloff(
                        SoundSystemConfig.ATTENUATION_LINEAR, 16.0f, 1.25f),
                0.0f);
        assertTrue(SoundPhysicsCompatibility.shouldEvaluateEnvironment(SoundSystemConfig.ATTENUATION_ROLLOFF));
    }

    @Test
    public void urlSourceOverwritePreservesNonAttenuatedCommand() throws MalformedURLException {
        TestSoundSystem system = new TestSoundSystem();
        system.newSource(
                false,
                "matmos-stereo",
                new URL("file:/test.ogg"),
                "test.ogg",
                false,
                0.0f,
                2048.0f,
                0.0f,
                SoundSystemConfig.ATTENUATION_NONE,
                16.0f);
        system.assertNonAttenuatedCommand();
    }

    @Test
    public void filenameSourceOverwritePreservesNonAttenuatedCommand() {
        TestSoundSystem system = new TestSoundSystem();
        system.newSource(
                false,
                "matmos-stereo",
                "test.ogg",
                false,
                0.0f,
                2048.0f,
                0.0f,
                SoundSystemConfig.ATTENUATION_NONE,
                16.0f);
        system.assertNonAttenuatedCommand();
    }

    private static final class TestSoundSystem extends MixinSoundSystem {
        private CommandObject command;

        private TestSoundSystem() {
            commandThread = new CommandThread(null) {
                @Override
                public void interrupt() {}
            };
        }

        @Override
        public boolean CommandQueue(CommandObject newCommand) {
            command = newCommand;
            return true;
        }

        private void assertNonAttenuatedCommand() {
            assertNotNull(command);
            assertEquals(SoundSystemConfig.ATTENUATION_NONE, command.intArgs[0]);
            assertEquals(16.0f, command.floatArgs[3], 0.0f);
        }
    }
}
