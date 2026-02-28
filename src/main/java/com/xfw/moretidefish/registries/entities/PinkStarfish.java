package com.xfw.moretidefish.registries.entities;

import com.li64.tide.registries.entities.fish.AbstractTideFish;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class PinkStarfish extends AbstractTideFish {
    public PinkStarfish(EntityType<? extends PinkStarfish> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean doesFlop() {
        return false;
    }
}