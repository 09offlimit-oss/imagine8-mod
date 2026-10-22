package com.imagine8;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> client packet that triggers the full-screen jumpscare. */
public record JumpscarePayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<JumpscarePayload> TYPE =
            new CustomPacketPayload.Type<>(Imagine8Mod.id("jumpscare"));
    public static final StreamCodec<RegistryFriendlyByteBuf, JumpscarePayload> CODEC =
            StreamCodec.unit(new JumpscarePayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
