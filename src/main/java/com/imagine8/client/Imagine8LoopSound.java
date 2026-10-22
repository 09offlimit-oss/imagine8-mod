package com.imagine8.client;

import com.imagine8.Imagine8Entity;
import com.imagine8.Imagine8Mod;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

/** Scary noise that loops and follows Imagine8 for as long as it exists. */
public class Imagine8LoopSound extends AbstractTickableSoundInstance {
    private final Imagine8Entity entity;

    public Imagine8LoopSound(Imagine8Entity entity) {
        super(Imagine8Mod.CHASE_LOOP, SoundSource.HOSTILE, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.looping = true;
        this.delay = 0;
        this.volume = 3.0f; // >1 extends hearing range (~48 blocks)
        this.pitch = 1.0f;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    @Override
    public void tick() {
        if (entity.isRemoved() || !entity.isAlive()) {
            this.stop();
            return;
        }
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }
}
