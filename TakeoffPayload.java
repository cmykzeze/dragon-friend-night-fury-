package com.httymd.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.httymd.HttymdMod;

/** Sent by the rider's client when they press Jump while sitting on a grounded Night Fury. */
public record TakeoffPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TakeoffPayload> TYPE =
            new CustomPacketPayload.Type<>(HttymdMod.id("takeoff"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TakeoffPayload> CODEC =
            StreamCodec.unit(new TakeoffPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
