package com.sonicether.soundphysics.mixins.early;

import com.sonicether.soundphysics.SoundPhysics;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundPoolEntry;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEventAccessorComposite.class)
public abstract class MixinSoundEventAccessorComposite {

    @Shadow
    public abstract ResourceLocation getSoundEventLocation();

    /** Each event a pick goes through, down to the sound, can ask for the sound to be muffled. */
    @Inject(method = "func_148720_g()Lnet/minecraft/client/audio/SoundPoolEntry;", at = @At("HEAD"))
    private void notePick(CallbackInfoReturnable<SoundPoolEntry> cir) {
        SoundPhysics.onPick(getSoundEventLocation());
    }
}
