package com.danielm.modid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SlimeMegaGrenadeEntity extends ThrowableItemProjectile {

    private int fuse = -1;
    private Entity stuckTo = null;

    public SlimeMegaGrenadeEntity(final EntityType<? extends SlimeMegaGrenadeEntity> type, final Level level) {
        super(type, level);
    }

    public SlimeMegaGrenadeEntity(final Level level, final LivingEntity thrower, final ItemStack stack) {
        super(SlimeMod.SLIME_MEGA_GRENADE_ENTITY, thrower, level, stack);
    }

    public SlimeMegaGrenadeEntity(final Level level, final double x, final double y, final double z, final ItemStack stack) {
        super(SlimeMod.SLIME_MEGA_GRENADE_ENTITY, x, y, z, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return SlimeMod.SLIME_MEGA_GRENADE;
    }

    @Override
    protected void onHitEntity(final EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!this.level().isClientSide() && this.fuse < 0) {
            this.stuckTo = hitResult.getEntity();
            this.arm();
        }
    }

    @Override
    protected void onHitBlock(final BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (!this.level().isClientSide() && this.fuse < 0) {
            this.arm();
        }
    }

    private void arm() {
        this.fuse = 200;                   // 10 seconds
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        if (this.fuse < 0) {
            super.tick();
            return;
        }
        if (this.stuckTo != null && this.stuckTo.isAlive()) {
            this.setPos(this.stuckTo.getX(),
                    this.stuckTo.getY() + this.stuckTo.getBbHeight() * 0.5,
                    this.stuckTo.getZ());
        }
        if (--this.fuse <= 0 && !this.level().isClientSide()) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                    16.0f, Level.ExplosionInteraction.TNT);

            BlockPos center = this.blockPosition();
            int radius = 12;
            for (BlockPos pos : BlockPos.betweenClosed(
                    center.offset(-radius, -radius, -radius),
                    center.offset(radius, radius, radius))) {
                if (center.distSqr(pos) <= radius * radius
                        && this.level().getBlockState(pos).is(Blocks.OBSIDIAN)) {
                    this.level().destroyBlock(pos, false);
                }
            }

            this.discard();
        }
    }
}