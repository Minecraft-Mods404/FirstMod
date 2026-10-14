package com.danielm.modid;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SlimeGrenadeEntity extends ThrowableItemProjectile {

    /** -1 means still flying. Once it lands or sticks, counts down to 0. */
    private int fuse = -1;

    /** The entity it stuck to, if any. Null when it landed on a block. */
    private Entity stuckTo = null;

    // ─── constructors ───

    /** Used by the game when reloading a chunk. */
    public SlimeGrenadeEntity(final EntityType<? extends SlimeGrenadeEntity> type, final Level level) {
        super(type, level);
    }

    /** Used when a player throws it. */
    public SlimeGrenadeEntity(final Level level, final LivingEntity thrower, final ItemStack stack) {
        super(SlimeMod.SLIME_GRENADE_ENTITY, thrower, level, stack);
    }

    /** Used by dispensers. */
    public SlimeGrenadeEntity(final Level level, final double x, final double y, final double z, final ItemStack stack) {
        super(SlimeMod.SLIME_GRENADE_ENTITY, x, y, z, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return SlimeMod.SLIME_GRENADE;
    }

    // ─── impact ───

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

    /** Stop moving and start the 3-second countdown. */
    private void arm() {
        this.fuse = 60;                    // 60 ticks = 3 seconds
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
    }

    // ─── per-tick behaviour ───

    @Override
    public void tick() {
        if (this.fuse < 0) {
            super.tick();                  // still in flight: normal projectile physics
            return;
        }

        // follow whatever it stuck to
        if (this.stuckTo != null && this.stuckTo.isAlive()) {
            this.setPos(this.stuckTo.getX(),
                    this.stuckTo.getY() + this.stuckTo.getBbHeight() * 0.5,
                    this.stuckTo.getZ());
        }

        if (--this.fuse <= 0 && !this.level().isClientSide()) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                    4.0f, Level.ExplosionInteraction.TNT);
            this.discard();
        }
    }
}