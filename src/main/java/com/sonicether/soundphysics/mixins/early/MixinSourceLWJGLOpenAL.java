package com.sonicether.soundphysics.mixins.early;

import com.sonicether.soundphysics.SoundPhysics;
import com.sonicether.soundphysics.SoundPhysicsCompatibility;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulscode.sound.Channel;
import paulscode.sound.FilenameURL;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.Source;
import paulscode.sound.libraries.ChannelLWJGLOpenAL;
import paulscode.sound.libraries.SourceLWJGLOpenAL;

@Mixin(value = SourceLWJGLOpenAL.class, remap = false)
public abstract class MixinSourceLWJGLOpenAL extends Source {
    @Shadow(remap = false)
    private ChannelLWJGLOpenAL channelOpenAL;

    @Unique
    private float soundphysics$referenceDistance = -1;

    public MixinSourceLWJGLOpenAL(boolean priority, boolean toStream, boolean toLoop, String sourcename, FilenameURL filenameURL, SoundBuffer soundBuffer, float x, float y, float z, int attModel, float distOrRoll, boolean temporary) {
        super(priority, toStream, toLoop, sourcename, filenameURL, soundBuffer, x, y, z, attModel, distOrRoll, temporary);
    }

    @Inject(method = "play(Lpaulscode/sound/Channel;)V",
            at = @At(value = "INVOKE", target = "Lpaulscode/sound/Channel;play()V", shift = At.Shift.AFTER, remap = false),
            remap = false)
    private void injectOnPlaySound(Channel c, CallbackInfo ci) {
        // Channels are reused, so every source sets its own reference distance (the range it asked for)
        if (soundphysics$referenceDistance < 0)
            soundphysics$referenceDistance = SoundPhysicsCompatibility.takeReferenceDistance(this.sourcename);
        AL10.alSourcef(this.channelOpenAL.ALSource.get(0), AL10.AL_REFERENCE_DISTANCE,
                this.attModel == SoundSystemConfig.ATTENUATION_ROLLOFF ? soundphysics$referenceDistance : 1.0F);
        SoundPhysics.onPlaySound(this.position.x, this.position.y, this.position.z, this.channelOpenAL.ALSource.get(0),
                this.attModel);
    }
}
