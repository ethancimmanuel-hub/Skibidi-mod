package net.skibiditoiletmod.entity.goal;

import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.LivingEntity;
import net.skibiditoiletmod.entity.SkibidiToiletEntity;

/**
 * Behaves like a normal melee goal, but the toilet keeps its distance a bit
 * more and "lunges" its head forward instead of walking fully into the
 * player's hitbox. The visual extension is driven by the entity's tracked
 * extension value (see SkibidiToiletEntity#tickExtension).
 */
public class ExtendHeadAttackGoal extends MeleeAttackGoal {

    private final SkibidiToiletEntity toilet;
    // Extra reach (in blocks, squared) granted by the extending head.
    private static final double EXTRA_REACH_SQUARED = 6.0D; // ~2.45 blocks of head extension

    public ExtendHeadAttackGoal(SkibidiToiletEntity toilet, double speed, boolean pauseWhenMobIdle) {
        super(toilet, speed, pauseWhenMobIdle);
        this.toilet = toilet;
    }

    @Override
    public boolean canStart() {
        return !toilet.isFlushing() && super.canStart();
    }

    @Override
    public boolean shouldContinue() {
        return !toilet.isFlushing() && super.shouldContinue();
    }

    @Override
    protected void attack(LivingEntity target, double squaredDistance) {
        double reach = this.getSquaredMaxAttackDistance(target) ;
        if (squaredDistance <= reach && this.getCooldown() <= 0) {
            this.resetCooldown();
            // Trigger the visual head-extension lunge on the entity.
            toilet.startHeadLunge();
            this.mob.swingHand(net.minecraft.util.Hand.MAIN_HAND);
            target.damage(this.mob.getDamageSources().mobAttack(this.mob), (float) this.mob.getAttributeValue(
                    net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE));
        }
    }

    @Override
    protected double getSquaredMaxAttackDistance(LivingEntity entity) {
        // Base melee reach plus the extending-head bonus.
        return this.mob.getWidth() * 2.0F * (this.mob.getWidth() * 2.0F)
                + entity.getWidth()
                + EXTRA_REACH_SQUARED;
    }
}
