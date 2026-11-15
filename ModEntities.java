package com.httymd.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

import com.httymd.HttymdMod;
import com.httymd.entity.NightFuryEntity;

public final class ModEntities {
    private ModEntities() {}

    /** Height (in blocks) of the rider's seat above the dragon's feet. Tweak this if the rider floats or sinks. */
    public static final float RIDER_SEAT_HEIGHT = 1.9f;

    public static final ResourceKey<EntityType<?>> NIGHT_FURY_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, HttymdMod.id("night_fury"));

    public static EntityType<NightFuryEntity> NIGHT_FURY;

    public static void register() {
        NIGHT_FURY = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                NIGHT_FURY_KEY,
                EntityType.Builder.of(NightFuryEntity::new, MobCategory.CREATURE)
                        .sized(1.7f, 2.25f)
                        .clientTrackingRange(10)
                        .fireImmune()
                        .passengerAttachments(RIDER_SEAT_HEIGHT)
                        .build(NIGHT_FURY_KEY));

        FabricDefaultAttributeRegistry.register(NIGHT_FURY, NightFuryEntity.createAttributes());
    }
}
