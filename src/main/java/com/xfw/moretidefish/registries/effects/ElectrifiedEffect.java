package com.xfw.moretidefish.registries.effects;

import com.xfw.moretidefish.registries.MTFMobEffectRegistry;
import com.xfw.moretidefish.registries.entities.ElectricEel;
import com.xfw.moretidefish.registries.particles.ZapParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class ElectrifiedEffect extends MobEffect {
    private static final double RANGE = 5.0;               //搜索半径
    private static final int MAX_TARGETS = 5;          //最多链接敌人

    public ElectrifiedEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        //每秒触发一次
        return true;
    }

    //电鳗不会导电
    public void onEffectStarted(LivingEntity livingEntity, int amplifier) {
        if(livingEntity instanceof ElectricEel electricEel){
            electricEel.removeEffect(MTFMobEffectRegistry.ELECTRIFIED);
        }
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        Level level = livingEntity.level();
        if (level.isClientSide) return true;
        long gameTime = level.getGameTime();
        if (gameTime % 8 == 0) {
            for (int i = 0; i < 2; i++){
                double startX = livingEntity.getX() + (livingEntity.getRandom().nextDouble() - 0.5) * livingEntity.getBbWidth();
                double startY = livingEntity.getY();
                double startZ = livingEntity.getZ() + (livingEntity.getRandom().nextDouble()- 0.5) * livingEntity.getBbWidth();
                Vec3 start = new Vec3(startX, startY, startZ);
                Vec3 end = livingEntity.getBoundingBox().getCenter();
                ((ServerLevel) level).sendParticles(new ZapParticleOption(end),
                        start.x, start.y, start.z, 1, 0, 0, 0, 0);
            }
        }
        if (gameTime % 20 == 0) {
            List<LivingEntity> targets = new ArrayList<>();
            Queue<LivingEntity> queue = new LinkedList<>();

            queue.add(livingEntity);
            targets.add(livingEntity);
            while (!queue.isEmpty() && targets.size() < MAX_TARGETS) {
                LivingEntity current = queue.poll();

                //搜索周围拥有此效果的其他敌人
                AABB searchBox = current.getBoundingBox().inflate(RANGE);
                List<LivingEntity> candidates = level.getEntitiesOfClass(
                        LivingEntity.class,
                        searchBox,
                        e -> e.isAlive()
                                && !targets.contains(e)
                                && e.hasEffect(MTFMobEffectRegistry.ELECTRIFIED)
                                && current.hasLineOfSight(e)
                );

                for (LivingEntity target : candidates) {
                    if (targets.size() >= MAX_TARGETS) break;
                    if (target.distanceToSqr(current) > RANGE * RANGE) continue;

                    targets.add(target);
                    queue.add(target);

                    Vec3 start = current.getBoundingBox().getCenter();
                    Vec3 end = target.getBoundingBox().getCenter();
                    ((ServerLevel) level).sendParticles(new ZapParticleOption(end),
                            start.x, start.y, start.z, 1, 0, 0, 0, 0);
                }
            }

            //伤害值 = 链住的敌人数，最少 targets.size()，最多MAX_TARGETS
            float damage = Math.min(targets.size(), MAX_TARGETS);
            //对所有目标伤害
            for (LivingEntity target : targets) {
                target.hurt(target.damageSources().lightningBolt(), damage);
            }
        }
        return true;
    }

    @Override
    public MobEffectCategory getCategory() {
        return super.getCategory();
    }

    @Override
    public int getColor() {
        return super.getColor();
    }
}