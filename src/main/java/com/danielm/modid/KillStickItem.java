package com.danielm.modid;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class KillStickItem extends Item {

    public KillStickItem(final Item.Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(final ItemStack stack, final LivingEntity target, final LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker.level() instanceof ServerLevel serverLevel) {
            target.kill(serverLevel);
        }
    }
}