package com.xfw.moretidefish.registries.item;

import com.xfw.moretidefish.registries.entities.PinkStarfishItemEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PinkStarfishItem extends Item {
    public PinkStarfishItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand).copy();

        if (!player.isCrouching()) {
            var entity = new PinkStarfishItemEntity(player, level, stack);
            float velocity = 1.5f;
            float inaccuracy = 1.0F;
            entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, inaccuracy);
            level.addFreshEntity(entity);
            player.swing(hand);
            player.level().playSound((Player)null , entity,SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);

            if (!player.isCreative()) {
                stack.shrink(1);
                player.getCooldowns().addCooldown(this, 3);
            }
        }else {
            player.startUsingItem(hand);
        }

        return InteractionResultHolder.consume(stack);
    }
}