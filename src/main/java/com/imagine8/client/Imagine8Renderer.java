package com.imagine8.client;

import com.imagine8.Imagine8Entity;
import com.imagine8.Imagine8Mod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Draws the PNG as a billboard that always faces the camera, so it looks at you from every angle. */
public class Imagine8Renderer extends EntityRenderer<Imagine8Entity> {
    public static final ResourceLocation TEXTURE = Imagine8Mod.id("textures/entity/imagine8.png");
    public static final int TEX_W = 411, TEX_H = 611;

    private static final float HEIGHT = 2.7f;
    private static final float WIDTH = HEIGHT * TEX_W / TEX_H;

    public Imagine8Renderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
    }

    @Override
    public ResourceLocation getTextureLocation(Imagine8Entity entity) {
        return TEXTURE;
    }

    @Override
    public void render(Imagine8Entity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float time = entity.tickCount + partialTick;
        float bob = (float) Math.sin(time * 0.15f) * 0.06f;
        poseStack.translate(0.0, HEIGHT / 2.0 + 0.05 + bob, 0.0);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0f));
        // Slight creepy flicker in size
        float flicker = 1.0f + (float) Math.sin(time * 1.7f) * 0.015f;
        poseStack.scale(flicker, flicker, flicker);

        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        float hw = WIDTH / 2f, hh = HEIGHT / 2f;
        vertex(vc, pose, -hw, -hh, 0f, 1f);
        vertex(vc, pose, hw, -hh, 1f, 1f);
        vertex(vc, pose, hw, hh, 1f, 0f);
        vertex(vc, pose, -hw, hh, 0f, 0f);
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float u, float v) {
        vc.addVertex(pose, x, y, 0f)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0f, 1f, 0f);
    }
}
