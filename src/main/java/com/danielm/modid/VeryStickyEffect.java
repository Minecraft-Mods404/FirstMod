package com.danielm.modid;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class VeryStickyEffect extends MobEffect {

    protected VeryStickyEffect(final MobEffectCategory category, final int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(final ServerLevel level, final LivingEntity entity, final int amplifier) {
        entity.hurtServer(level, entity.damageSources().magic(), 5.0F);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(final int duration, final int amplifier) {
        int interval = 25 >> amplifier;        // same cadence as poison
        return interval <= 0 || duration % interval == 0;
    }
}