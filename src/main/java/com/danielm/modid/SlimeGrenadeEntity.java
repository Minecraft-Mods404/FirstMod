package com.danielm.modid;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class SlimeGrenadeEntity extends ThrowableItemProjectile {

    public SlimeGrenadeEntity(final EntityType<? extends SlimeGrenadeEntity> type, final Level level) {
        super(type, level);
    }

    @Override
    protected Item getDefaultItem() {
        return FirstMod.SLIME_GRENADE;
    }

    @Override
    protected void onHit(final HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide()) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                    4.0f, Level.ExplosionInteraction.TNT);
            this.discard();
        }
    }
}