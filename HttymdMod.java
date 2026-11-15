package com.httymd;

import net.minecraft.resources.Identifier;

import net.fabricmc.api.ModInitializer;

import com.httymd.hatching.DragonHatching;
import com.httymd.network.DragonNetworking;
import com.httymd.registry.ModEntities;

/**
 * HTTYMD: Night Fury - Fabric port.
 * Original mod, model, animations and textures: The HTTYMD Team (used with permission, see CREDITS.md).
 */
public class HttymdMod implements ModInitializer {
    public static final String MOD_ID = "httymd";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModEntities.register();
        DragonNetworking.registerCommon();
        DragonHatching.register();
    }
}
