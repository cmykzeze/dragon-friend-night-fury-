package com.httymd.network;

import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import com.httymd.entity.NightFuryEntity;

public final class DragonNetworking {
    private DragonNetworking() {}

    public static void registerCommon() {
        PayloadTypeRegistry.serverboundPlay().register(TakeoffPayload.TYPE, TakeoffPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TakeoffPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.getVehicle() instanceof NightFuryEntity dragon) {
                dragon.onRiderTakeoff(player);
            }
        });
    }
}
