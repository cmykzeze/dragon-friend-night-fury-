package com.httymd.hatching;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import com.httymd.entity.NightFuryEntity;
import com.httymd.registry.ModEntities;

/**
 * The only way to get a Night Fury: right-click a placed Dragon Egg with a bottle of Dragon's Breath.
 * The egg shakes and glows for a few seconds, then hatches into a Night Fury tamed to you.
 */
public final class DragonHatching {
    private DragonHatching() {}

    /** 8 seconds. */
    private static final int HATCH_TICKS = 160;

    private static final class Pending {
        final ServerLevel level;
        final BlockPos pos;
        final UUID owner;
        int ticks = 0;

        Pending(ServerLevel level, BlockPos pos, UUID owner) {
            this.level = level;
            this.pos = pos;
            this.owner = owner;
        }
    }

    private static final List<Pending> PENDING = new ArrayList<>();

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(Items.DRAGON_BREATH)) {
                return InteractionResult.PASS;
            }
            BlockPos pos = hit.getBlockPos();
            if (!level.getBlockState(pos).is(Blocks.DRAGON_EGG)) {
                return InteractionResult.PASS;
            }
            if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
                // Claim the click on the client too, so the egg does not teleport.
                return InteractionResult.SUCCESS;
            }
            for (Pending p : PENDING) {
                if (p.level == serverLevel && p.pos.equals(pos)) {
                    return InteractionResult.SUCCESS; // already hatching
                }
            }
            if (!player.hasInfiniteMaterials()) {
                stack.shrink(1);
                player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
            }
            PENDING.add(new Pending(serverLevel, pos.immutable(), player.getUUID()));
            serverLevel.playSound(null, pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.NEUTRAL, 0.6f, 1.4f);
            return InteractionResult.SUCCESS;
        });

        ServerTickEvents.END_SERVER_TICK.register(DragonHatching::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PENDING.clear());
    }

    private static void tick(MinecraftServer server) {
        Iterator<Pending> it = PENDING.iterator();
        while (it.hasNext()) {
            Pending p = it.next();
            if (!p.level.getBlockState(p.pos).is(Blocks.DRAGON_EGG)) {
                it.remove(); // egg was broken or fell
                continue;
            }
            p.ticks++;
            Vec3 c = Vec3.atCenterOf(p.pos);
            if (p.ticks % 3 == 0) {
                p.level.sendParticles(ParticleTypes.PORTAL, c.x, c.y, c.z, 8, 0.3, 0.4, 0.3, 0.3);
            }
            if (p.ticks % 20 == 0) {
                p.level.sendParticles(ParticleTypes.END_ROD, c.x, c.y + 0.5, c.z, 6, 0.2, 0.3, 0.2, 0.05);
                p.level.playSound(null, p.pos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.NEUTRAL, 1.0f, 0.6f);
            }
            if (p.ticks >= HATCH_TICKS) {
                hatch(p);
                it.remove();
            }
        }
    }

    private static void hatch(Pending p) {
        ServerLevel level = p.level;
        Vec3 c = Vec3.atBottomCenterOf(p.pos);

        NightFuryEntity dragon = ModEntities.NIGHT_FURY.create(level, EntitySpawnReason.TRIGGERED);
        if (dragon == null) {
            return;
        }
        dragon.setPos(c.x, c.y, c.z);
        dragon.setYRot(level.getRandom().nextFloat() * 360.0f);

        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(p.owner);
        if (owner != null) {
            dragon.tame(owner);
        }

        level.removeBlock(p.pos, false);
        level.addFreshEntity(dragon);

        level.sendParticles(ParticleTypes.PORTAL, c.x, c.y + 1.0, c.z, 60, 0.6, 0.8, 0.6, 0.5);
        level.playSound(null, p.pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.NEUTRAL, 1.0f, 1.0f);
    }
}
