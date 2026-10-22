package com.imagine8.client;

import com.imagine8.Imagine8Mod;
import com.imagine8.JumpscarePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

public class Imagine8Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Imagine8Mod.IMAGINE8, Imagine8Renderer::new);

        Imagine8Mod.CLIENT_LOOP_STARTER = entity ->
                Minecraft.getInstance().getSoundManager().play(new Imagine8LoopSound(entity));

        ClientPlayNetworking.registerGlobalReceiver(JumpscarePayload.TYPE, (payload, context) -> JumpscareOverlay.trigger());
        ClientTickEvents.END_CLIENT_TICK.register(client -> JumpscareOverlay.tick());
        HudRenderCallback.EVENT.register(JumpscareOverlay::render);
    }
}
