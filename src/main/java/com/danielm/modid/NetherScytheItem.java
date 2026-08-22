package com.danielm.modid;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class NetherScytheItem extends Item {

    /** How far from the player the sweep reaches. */
    public static final double SWEEP_RADIUS = 8.0;

    public NetherScytheItem(final Item.Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(final ItemStack stack, final LivingEntity target, final LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);

        if (attacker.level().isClientSide()) return;
        ServerLevel level = (ServerLevel) attacker.level();

        AABB box = attacker.getBoundingBox().inflate(SWEEP_RADIUS);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, box);

        for (LivingEntity victim : nearby) {
            if (victim == attacker || victim == target) continue;
            if (!victim.isAlive()) continue;
            victim.hurtServer(level, attacker.damageSources().playerAttack(
                    (net.minecraft.world.entity.player.Player) attacker), 2048.0F);
        }
    }
}