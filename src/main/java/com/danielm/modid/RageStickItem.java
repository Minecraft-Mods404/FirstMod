package com.danielm.modid;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RageStickItem extends Item {

    /** First mob picked, per player, waiting for a second. */
    private static final Map<UUID, Mob> PENDING = new HashMap<>();

    public RageStickItem(final Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(final ItemStack stack, final Player player,
                                                  final LivingEntity target, final InteractionHand hand) {
        if (!(player.level() instanceof ServerLevel serverLevel) || !(target instanceof Mob mob)) {
            return InteractionResult.PASS;
        }

        Mob first = PENDING.remove(player.getUUID());

        if (first == null) {
            PENDING.put(player.getUUID(), mob);
            // marked: a few small flames
            spawnAt(serverLevel, mob, ParticleTypes.FLAME, 8);
        } else if (first != mob && first.isAlive()) {
            first.setTarget(mob);
            mob.setTarget(first);
            // engaged: angry particles on both
            spawnAt(serverLevel, first, ParticleTypes.ANGRY_VILLAGER, 12);
            spawnAt(serverLevel, mob, ParticleTypes.ANGRY_VILLAGER, 12);
        }

        return InteractionResult.SUCCESS;
    }

    private static void spawnAt(final ServerLevel level, final Mob mob,
                                final net.minecraft.core.particles.SimpleParticleType type, final int count) {
        level.sendParticles(type,
                mob.getX(),
                mob.getY() + mob.getBbHeight() * 0.7,
                mob.getZ(),
                count,
                0.4, 0.4, 0.4,
                0.02);
    }
}