package com.imagine8.client;

import com.imagine8.Imagine8Mod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

import java.util.Random;

/** Full-screen shaking, flashing jumpscare with a loud scream. */
public class JumpscareOverlay {
    private static final int DURATION = 50; // ticks (2.5 s)
    private static int ticks = 0;
    private static final Random RAND = new Random();

    public static void trigger() {
        ticks = DURATION;
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(Imagine8Mod.JUMPSCARE, 1.0f, 1.0f));
    }

    public static void tick() {
        if (ticks > 0) ticks--;
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        if (ticks <= 0) return;
        int sw = g.guiWidth(), sh = g.guiHeight();
        float progress = 1f - (ticks - delta.getGameTimeDeltaPartialTick(false)) / DURATION;

        boolean flash = (ticks / 2) % 2 == 0;
        g.fill(0, 0, sw, sh, flash ? 0xFF3A0000 : 0xFF000000);

        float scale = 0.75f + Math.min(progress * 6f, 1f) * 0.8f + progress * 0.2f;
        int drawH = (int) (sh * scale);
        int drawW = (int) (drawH * ((float) Imagine8Renderer.TEX_W / Imagine8Renderer.TEX_H));
        int shake = 6 + (int) (progress * 20);
        int x = (sw - drawW) / 2 + RAND.nextInt(shake * 2 + 1) - shake;
        int y = (sh - drawH) / 2 + RAND.nextInt(shake * 2 + 1) - shake;
        g.blit(Imagine8Renderer.TEXTURE, x, y, drawW, drawH, 0f, 0f,
                Imagine8Renderer.TEX_W, Imagine8Renderer.TEX_H, Imagine8Renderer.TEX_W, Imagine8Renderer.TEX_H);
    }
}
