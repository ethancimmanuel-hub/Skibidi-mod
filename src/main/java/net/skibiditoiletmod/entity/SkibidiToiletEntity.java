package net.skibiditoiletmod.entity;

import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * A hostile mob that lunges at the player by "extending" its head (the
 * actual reach/lunge logic lives in ExtendHeadAttackGoal + tickExtension
 * here; the visual stretch is applied client-side in the renderer using
 * the HEAD_EXTENSION tracked value).
 *
 * Right-clicking it starts the "flush" sequence: the head spins rapidly
 * for FLUSH_DURATION_TICKS, particles + sound play, then the entity dies.
 */
public class SkibidiToiletEntity extends HostileEntity {

    private static final TrackedData<Float> HEAD_EXTENSION =
            DataTracker.registerData(SkibidiToiletEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Boolean> FLUSHING =
            DataTracker.registerData(SkibidiToiletEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Float> SPIN_ANGLE =
            DataTracker.registerData(SkibidiToiletEntity.class, TrackedDataHandlerRegistry.FLOAT);

    /** How long, in ticks, the flush animation plays before the mob dies. */
    public static final int FLUSH_DURATION_TICKS = 30; // 1.5 seconds

    /** How many ticks the head stays lunged forward after an attack. */
    private static final int LUNGE_DURATION_TICKS = 8;

    private int lungeTicksLeft = 0;
    private int flushTicksLeft = 0;

    public SkibidiToiletEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.experiencePoints = 6;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new net.skibiditoiletmod.entity.goal.ExtendHeadAttackGoal(this, 1.0D, false));
        this.goalSelector.add(2, new WanderAroundFarGoal(this, 0.8D));
        this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 12.0F));
        this.goalSelector.add(4, new LookAroundGoal(this));

        this.targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(HEAD_EXTENSION, 0.0F);
        this.dataTracker.startTracking(FLUSHING, false);
        this.dataTracker.startTracking(SPIN_ANGLE, 0.0F);
    }

    // ---------------------------------------------------------------
    // Head lunge (attack) logic
    // ---------------------------------------------------------------

    /** Called by ExtendHeadAttackGoal when the toilet lands/attempts a hit. */
    public void startHeadLunge() {
        this.lungeTicksLeft = LUNGE_DURATION_TICKS;
        if (!this.getWorld().isClient) {
            this.playSound(SoundEvents.ENTITY_HOSTILE_BIG_FALL, 1.0F, 0.6F);
        }
    }

    public boolean isFlushing() {
        return this.dataTracker.get(FLUSHING);
    }

    /** 0.0 = head retracted, 1.0 = fully extended toward the target. Used by the renderer. */
    public float getHeadExtension(float tickDelta) {
        return this.dataTracker.get(HEAD_EXTENSION);
    }

    /** Accumulated spin angle in degrees, used by the renderer while flushing. */
    public float getSpinAngle(float tickDelta) {
        return this.dataTracker.get(SPIN_ANGLE);
    }

    // ---------------------------------------------------------------
    // Right-click to flush
    // ---------------------------------------------------------------

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (this.isFlushing() || this.isRemoved()) {
            return super.interactMob(player, hand);
        }
        if (!this.getWorld().isClient) {
            beginFlush();
        }
        return ActionResult.success(this.getWorld().isClient);
    }

    private void beginFlush() {
        this.dataTracker.set(FLUSHING, true);
        this.flushTicksLeft = FLUSH_DURATION_TICKS;
        // Stop it from wandering/attacking while it goes down the drain.
        this.getNavigation().stop();
        this.setTarget(null);
        this.playSound(SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, 1.5F, 1.0F);
        this.playSound(SoundEvents.ENTITY_GENERIC_SPLASH, 1.0F, 1.2F);

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.SPLASH,
                    this.getX(), this.getY() + this.getHeight() * 0.5, this.getZ(),
                    20, 0.3, 0.3, 0.3, 0.05);
            serverWorld.spawnParticles(ParticleTypes.BUBBLE_COLUMN_UP,
                    this.getX(), this.getY(), this.getZ(),
                    15, 0.25, 0.4, 0.25, 0.02);
        }
    }

    // ---------------------------------------------------------------
    // Tick logic
    // ---------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();

        if (this.isFlushing()) {
            tickFlush();
        } else {
            tickLungeDecay();
        }
    }

    private void tickLungeDecay() {
        float current = this.dataTracker.get(HEAD_EXTENSION);
        float target;
        if (lungeTicksLeft > 0) {
            lungeTicksLeft--;
            target = 1.0F;
        } else {
            target = 0.0F;
        }
        // Smoothly approach the target extension amount.
        float next = current + (target - current) * 0.5F;
        if (Math.abs(next - target) < 0.01F) {
            next = target;
        }
        if (next != current) {
            this.dataTracker.set(HEAD_EXTENSION, next);
        }
    }

    private void tickFlush() {
        // Spin faster and faster as it goes down, like water circling a drain.
        int elapsed = FLUSH_DURATION_TICKS - flushTicksLeft;
        float spinSpeed = 15.0F + elapsed * 6.0F; // degrees per tick, accelerating
        float newAngle = (this.dataTracker.get(SPIN_ANGLE) + spinSpeed) % 360.0F;
        this.dataTracker.set(SPIN_ANGLE, newAngle);

        // Also visually "sink" and shrink a little by re-using head extension
        // as a retract-into-the-bowl amount (renderer interprets it during flush).
        this.dataTracker.set(HEAD_EXTENSION, Math.max(0.0F, (float) flushTicksLeft / FLUSH_DURATION_TICKS));

        if (!this.getWorld().isClient) {
            Random random = this.getRandom();
            if (this.getWorld() instanceof ServerWorld serverWorld && flushTicksLeft % 3 == 0) {
                serverWorld.spawnParticles(ParticleTypes.BUBBLE,
                        this.getX() + random.nextGaussian() * 0.2, this.getY() + 0.2, this.getZ() + random.nextGaussian() * 0.2,
                        3, 0.1, 0.1, 0.1, 0.01);
            }

            flushTicksLeft--;
            if (flushTicksLeft <= 0) {
                this.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0F, 1.4F);
                this.discard(); // fully removed, no death animation/loot corpse lingering
            }
        }
    }

    @Override
    protected void dropLoot(net.minecraft.entity.damage.DamageSource source, boolean causedByPlayer) {
        // No loot when flushed away; still allow normal loot if killed by combat/damage instead.
        if (!this.isFlushing()) {
            super.dropLoot(source, causedByPlayer);
        }
    }

    @Override
    public boolean canBeLeashedBy(PlayerEntity player) {
        return false;
    }

    @Override
    protected float getActiveEyeHeight(net.minecraft.entity.EntityPose pose, net.minecraft.entity.EntityDimensions dimensions) {
        return dimensions.height * 0.8F;
    }
}
