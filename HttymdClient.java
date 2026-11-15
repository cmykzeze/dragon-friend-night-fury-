package com.httymd.client;

import net.minecraft.client.Minecraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

import com.httymd.client.model.NightFuryModel;
import com.httymd.client.model.NightFuryModelData;
import com.httymd.entity.NightFuryEntity;
import com.httymd.network.TakeoffPayload;
import com.httymd.registry.ModEntities;

public class HttymdClient implements ClientModInitializer {
    private static boolean jumpWasDown = false;

    @Override
    public void onInitializeClient() {
        ModelLayerRegistry.registerModelLayer(NightFuryModel.LAYER, NightFuryModelData::createLayer);
        EntityRendererRegistry.register(ModEntities.NIGHT_FURY, NightFuryRenderer::new);

        // Jump while riding a grounded Night Fury = take off.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Minecraft mc = client;
            if (mc.player == null) {
                jumpWasDown = false;
                return;
            }
            boolean down = mc.options.keyJump.isDown();
            if (down && !jumpWasDown
                    && mc.player.getVehicle() instanceof NightFuryEntity dragon
                    && !dragon.isFlying()) {
                ClientPlayNetworking.send(new TakeoffPayload());
            }
            jumpWasDown = down;
        });
    }
}
