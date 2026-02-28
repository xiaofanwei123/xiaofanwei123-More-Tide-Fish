package com.xfw.moretidefish.registries.item;

import com.li64.tide.registries.TideEntityTypes;
import com.xfw.moretidefish.misc.WaterIgnoringDamageCalculator;
import com.xfw.moretidefish.registries.MTFParticleRegistry;
import com.xfw.moretidefish.registries.entities.PufferfishBomb;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PufferfishBombItem extends Item {
    public PufferfishBombItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);
        if (!player.isCrouching()) {
            EntityType<?> entityType = TideEntityTypes.ENTITY_TYPES.get("moretidefish_pufferfish_bomb");
            PufferfishBomb bomb = new PufferfishBomb((EntityType<PufferfishBomb>) entityType, level);
            Vec3 look = player.getLookAngle();
            Vec3 spawnPos = player.getEyePosition().add(look.x * 0.5, look.y * 0.5, look.z * 0.5);
            bomb.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            bomb.setYRot(player.getYRot());
            bomb.setDeltaMovement(look.x * 1.5, look.y * 1.5, look.z * 1.5);
            bomb.setPuffState(2);
            bomb.setInflateCounter(player.getRandom().nextBoolean() ? 19 : 20);
            level.addFreshEntity(bomb);
            if (!player.isCreative()) {
                itemstack.shrink(1);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1F, 1F);
        } else {
            player.startUsingItem(usedHand);
        }
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack itemStack = super.finishUsingItem(stack, level, livingEntity);
        ExplosionDamageCalculator calculator = new WaterIgnoringDamageCalculator();
        livingEntity.level().explode(null, level.damageSources().explosion(null), calculator,
                livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), 3, false,
                Level.ExplosionInteraction.NONE,
                MTFParticleRegistry.AIR_BUBBLE.get(), MTFParticleRegistry.AIR_BUBBLE.get(),
                SoundEvents.GENERIC_EXPLODE);
        return itemStack;
    }

    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        int eatTime = super.getUseDuration(stack, entity);
        return eatTime*5;
    }

}
