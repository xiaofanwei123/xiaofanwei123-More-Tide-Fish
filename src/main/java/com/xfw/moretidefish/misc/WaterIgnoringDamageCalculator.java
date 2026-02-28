package com.xfw.moretidefish.misc;

import com.xfw.moretidefish.registries.entities.PufferfishBomb;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;

public class WaterIgnoringDamageCalculator extends ExplosionDamageCalculator {
    @Override
    public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, FluidState fluid) {
        if (state.is(Blocks.WATER) || fluid.is(FluidTags.WATER)) {
            return Optional.of(0.0F);
        }
        return super.getBlockExplosionResistance(explosion, reader, pos, state, fluid);
    }

    @Override
    public boolean shouldBlockExplode(Explosion explosion, BlockGetter reader, BlockPos pos, BlockState state, float power) {
        //水不被破坏
        if (state.is(Blocks.WATER) || state.getFluidState().is(FluidTags.WATER)) {
            return false;
        }
        return super.shouldBlockExplode(explosion, reader, pos, state, power);
    }

    @Override
    public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
        //只排自身
        if (entity instanceof PufferfishBomb) {
            return false;
        }
        return super.shouldDamageEntity(explosion, entity);
    }
}