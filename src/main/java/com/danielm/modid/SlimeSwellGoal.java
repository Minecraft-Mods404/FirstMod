package com.danielm.modid;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class SlimeSwellGoal extends Goal {

    private final ExplosiveSlimeEntity slime;
    private LivingEntity target;

    private static final TargetingConditions SWELL_TARGETING =
            TargetingConditions.forCombat().range(3.0);

    public SlimeSwellGoal(final ExplosiveSlimeEntity slime) {
        this.slime = slime;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity current = this.slime.getTarget();
        return this.slime.getSwellDir() > 0
                || (current != null && this.slime.distanceToSqr(current) < 9.0);
    }

    @Override
    public void start() {
        this.slime.getNavigation().stop();
        this.target = this.slime.getTarget();
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.target == null) {
            this.slime.setSwellDir(-1);
        } else if (this.slime.distanceToSqr(this.target) > 49.0) {
            this.slime.setSwellDir(-1);
        } else if (!this.slime.getSensing().hasLineOfSight(this.target)) {
            this.slime.setSwellDir(-1);
        } else {
            this.slime.setSwellDir(1);
        }
    }
}