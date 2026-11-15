package com.httymd.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The friendly, rideable Night Fury.
 *
 * Behaviour is modelled on the original HTTYMD dragon (tame, follows its owner, defends its owner,
 * rideable, flies) but rebuilt on the current Minecraft entity system.
 *
 * Controls: right-click your own Night Fury to mount. Press Jump on the ground to take off. While flying
 * the dragon goes where you look (W/S/A/D to move). It lands by itself when it touches the ground.
 * Sneak + right-click (empty or any item) to make it sit/stand. Feed fish to heal it.
 */
public class NightFuryEntity extends TamableAnimal {
    private static final EntityDataAccessor<Boolean> FLYING =
            SynchedEntityData.defineId(NightFuryEntity.class, EntityDataSerializers.BOOLEAN);

    /** Blocks per tick at full throttle. */
    private static final double FLY_SPEED = 0.75;
    /** For this many ticks after take-off the dragon gets an extra upward push. */
    private static final int TAKEOFF_LIFT_TICKS = 14;

    private int flightTicks = 0;
    private float flyAnim = 0f;
    private float flyAnimO = 0f;

    public NightFuryEntity(EntityType<? extends NightFuryEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.0)
                // The dragon never takes fall damage.
                .add(Attributes.SAFE_FALL_DISTANCE, 1024.0)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLYING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.1, 10.0f, 3.0f));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }

    // ------------------------------------------------------------------ state

    public boolean isFlying() {
        return this.entityData.get(FLYING);
    }

    private void setFlying(boolean flying) {
        this.entityData.set(FLYING, flying);
        this.setNoGravity(flying);
    }

    /** 0 = fully on the ground pose, 1 = fully flying pose (smoothed for rendering). */
    public float getFlyBlend(float partialTick) {
        return Mth.lerp(partialTick, this.flyAnimO, this.flyAnim);
    }

    /** Called on the server when the rider presses Jump. */
    public void onRiderTakeoff(Player rider) {
        if (this.getControllingPassenger() == rider && !this.isFlying() && !this.isInWater()) {
            this.setFlying(true);
            this.flightTicks = 0;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        this.flyAnimO = this.flyAnim;
        this.flyAnim = Mth.clamp(this.flyAnim + (this.isFlying() ? 0.08f : -0.08f), 0.0f, 1.0f);
        this.flightTicks = this.isFlying() ? this.flightTicks + 1 : 0;

        if (!this.level().isClientSide() && this.isFlying()) {
            if (this.getControllingPassenger() == null) {
                // Rider got off: come down (no fall damage).
                this.setFlying(false);
            } else if (this.flightTicks > 20 && (this.onGround() || this.isInWater())) {
                this.setFlying(false);
            }
        }
    }

    // ------------------------------------------------------------------ riding

    @Override
    public LivingEntity getControllingPassenger() {
        Entity first = this.getFirstPassenger();
        if (first instanceof Player player && this.isTame() && this.isOwnedBy(player)) {
            return player;
        }
        return null;
    }

    @Override
    public void travel(Vec3 input) {
        LivingEntity rider = this.getControllingPassenger();
        if (rider == null || !this.isAlive()) {
            super.travel(input);
            return;
        }

        // Face where the rider is looking.
        this.setYRot(rider.getYRot());
        this.yRotO = this.getYRot();
        this.setXRot(rider.getXRot() * 0.5f);
        this.setRot(this.getYRot(), this.getXRot());
        this.setYBodyRot(this.getYRot());
        this.setYHeadRot(this.getYRot());

        float strafe = rider.xxa * 0.5f;
        float forward = rider.zza;
        if (forward <= 0.0f) {
            forward *= 0.25f;
        }

        if (!this.isControlledByLocalInstance()) {
            return;
        }

        if (this.isFlying()) {
            this.flyTravel(rider, strafe, forward);
        } else {
            this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
            super.travel(new Vec3(strafe, input.y, forward));
        }
    }

    private void flyTravel(LivingEntity rider, float strafe, float forward) {
        Vec3 look = rider.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0.0, look.x);
        if (right.lengthSqr() > 1.0E-4) {
            right = right.normalize();
        }

        Vec3 target = look.scale(forward * FLY_SPEED).add(right.scale(-strafe * FLY_SPEED));
        if (this.flightTicks < TAKEOFF_LIFT_TICKS) {
            target = target.add(0.0, 0.45, 0.0);
        }

        Vec3 current = this.getDeltaMovement();
        this.setDeltaMovement(current.add(target.subtract(current).scale(0.12)));
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    // ------------------------------------------------------------------ interaction

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.isTame() && this.isOwnedBy(player)) {
            if (stack.is(ItemTags.FISHES) && this.getHealth() < this.getMaxHealth()) {
                if (!this.level().isClientSide()) {
                    this.heal(6.0f);
                    stack.consume(1, player);
                }
                return InteractionResult.SUCCESS;
            }

            if (player.isSecondaryUseActive()) {
                if (!this.level().isClientSide()) {
                    boolean sit = !this.isOrderedToSit();
                    this.setOrderedToSit(sit);
                    this.setInSittingPose(sit);
                    this.getNavigation().stop();
                    this.setTarget(null);
                }
                return InteractionResult.SUCCESS;
            }

            if (!this.isVehicle()) {
                if (!this.level().isClientSide()) {
                    this.setOrderedToSit(false);
                    this.setInSittingPose(false);
                    player.startRiding(this);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    // ------------------------------------------------------------------ misc

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.FISHES);
    }

    @Override
    public boolean canMate(Animal other) {
        return false;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }
}
