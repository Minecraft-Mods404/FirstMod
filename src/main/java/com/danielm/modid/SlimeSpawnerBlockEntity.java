package com.danielm.modid;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SlimeSpawnerBlockEntity extends BlockEntity {

    /** Ticks of spawning one Super Slime buys. 1200 = 1 minute. */
    public static final int TICKS_PER_FUEL = 1200;

    /** How often a slime appears while running. 100 = 5 seconds. */
    public static final int SPAWN_INTERVAL = 100;

    /** Most Super Slime the spawner will hold. */
    public static final int MAX_FUEL = 99;

    /** Super Slime remaining. */
    private int fuel = 0;

    /** Whether the player has switched it on. */
    private boolean active = false;

    /** Counts down to the next fuel unit being consumed. */
    private int fuelTimer = 0;

    /** Counts down to the next slime. */
    private int spawnTimer = 0;

    public SlimeSpawnerBlockEntity(final BlockPos pos, final BlockState state) {
        super(SlimeMod.SLIME_SPAWNER_ENTITY, pos, state);
    }

    // ─── state, read by the block ───

    public int getFuel() {
        return this.fuel;
    }

    public boolean isActive() {
        return this.active;
    }

    /** Returns false if it is already full. */
    public boolean addFuel(final int amount) {
        if (this.fuel >= MAX_FUEL) {
            return false;
        }
        this.fuel = Math.min(MAX_FUEL, this.fuel + amount);
        this.setChanged();
        updateFuelledState();
        return true;
    }

    /** Returns the new state. Refuses to start with no fuel. */
    public boolean toggle() {
        if (!this.active && this.fuel <= 0) {
            return false;
        }
        this.active = !this.active;
        if (this.active) {
            this.fuelTimer = TICKS_PER_FUEL;
            this.spawnTimer = SPAWN_INTERVAL;
        }
        this.setChanged();
        return this.active;
    }

    private void updateFuelledState() {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.setBlock(this.worldPosition,
                    this.getBlockState().setValue(SlimeSpawnerBlock.FUELLED, this.fuel > 0), 3);
        }
    }

    // ─── the tick ───

    public static void serverTick(final Level level, final BlockPos pos,
                                  final BlockState state, final SlimeSpawnerBlockEntity be) {

        if (!be.active || be.fuel <= 0 || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // burn a Super Slime once a minute
        if (--be.fuelTimer <= 0) {
            be.fuel--;
            be.fuelTimer = TICKS_PER_FUEL;
            be.setChanged();

            if (be.fuel <= 0) {
                be.active = false;
                be.updateFuelledState();
                return;
            }
        }

        // a slime every 5 seconds
        if (--be.spawnTimer <= 0) {
            be.spawnTimer = SPAWN_INTERVAL;
            spawnSlime(serverLevel, pos);
        }
    }

    private static void spawnSlime(final ServerLevel level, final BlockPos pos) {
        Slime slime = net.minecraft.world.entity.EntityTypes.SLIME.create(
                level, EntitySpawnReason.SPAWNER);
        if (slime == null) {
            return;
        }

        // small, medium or large - never a Prince or King
        int size = switch (level.getRandom().nextInt(3)) {
            case 0 -> 1;
            case 1 -> 2;
            default -> 4;
        };
        slime.setSize(size, true);

        // drop it in just above the block, nudged off centre
        double x = pos.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 1.5;
        double z = pos.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 1.5;
        slime.snapTo(x, pos.getY() + 1.0, z, level.getRandom().nextFloat() * 360.0F, 0.0F);

        level.addFreshEntity(slime);
    }

    // ─── persistence ───

    @Override
    protected void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("fuel", this.fuel);
        output.putBoolean("active", this.active);
        output.putInt("fuelTimer", this.fuelTimer);
        output.putInt("spawnTimer", this.spawnTimer);
    }

    @Override
    protected void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);
        this.fuel = input.getIntOr("fuel", 0);
        this.active = input.getBooleanOr("active", false);
        this.fuelTimer = input.getIntOr("fuelTimer", TICKS_PER_FUEL);
        this.spawnTimer = input.getIntOr("spawnTimer", SPAWN_INTERVAL);
    }
}