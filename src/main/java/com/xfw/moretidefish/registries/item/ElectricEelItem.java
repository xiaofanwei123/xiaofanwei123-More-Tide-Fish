package com.xfw.moretidefish.registries.item;


import com.xfw.moretidefish.registries.MTFDataAttachmentsRegistry;
import com.xfw.moretidefish.registries.MTFMobEffectRegistry;
import com.xfw.moretidefish.registries.MTFParticleRegistry;
import com.xfw.moretidefish.registries.entities.ElectricEelWhip;
import com.xfw.moretidefish.registries.MTFSoundRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ElectricEelItem extends TieredItem {

    public ElectricEelItem(Properties properties) {
        super(Tiers.IRON,properties.stacksTo(1).component(DataComponents.ATTRIBUTE_MODIFIERS,createAttributes(4.0f)));
    }

    public static ItemAttributeModifiers createAttributes(float damage) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage-1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @NotNull
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isCrouching()) {
            if (hand == InteractionHand.MAIN_HAND && !level.isClientSide && !(level.getEntity(player.getData(MTFDataAttachmentsRegistry.WHIP)) instanceof ElectricEelWhip)) {
                ElectricEelWhip whip = new ElectricEelWhip(level, player, stack);
                level.addFreshEntity(whip);
                whip.playSound(MTFSoundRegistry.ELECTRIC_EEL_WHIP.get(), 1.0F, 1.0F / (player.getRandom().nextFloat() * 0.4F + 0.8F));
                player.awardStat(Stats.ITEM_USED.get(this));
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            return InteractionResultHolder.pass(stack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
    }

    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        int eatTime = super.getUseDuration(stack, entity);
        return eatTime*5;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack itemStack = super.finishUsingItem(stack, level, livingEntity);
        livingEntity.addEffect(new MobEffectInstance(MTFMobEffectRegistry.ELECTRIFIED, 60, 0, false, false));
        return itemStack;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        doPostHurtEffects(target);
    }

    public void doPostHurtEffects(LivingEntity livingEntity) {
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            livingEntity.addEffect(new MobEffectInstance(MTFMobEffectRegistry.ELECTRIFIED, 200, 0, false, false));
            double x = livingEntity.getX() + (livingEntity.getRandom().nextFloat() - 0.5) * livingEntity.getBbWidth();
            double y = livingEntity.getY() + livingEntity.getRandom().nextFloat() * livingEntity.getBbHeight();
            double z = livingEntity.getZ() + (livingEntity.getRandom().nextFloat() - 0.5) * livingEntity.getBbWidth();
            for (int i = 0; i < 4; i++) {
                serverLevel.sendParticles(MTFParticleRegistry.SPARK_PARTICLE.get(), x, y, z, 1, 0, 0, 0, 0.1);
            }
        }
    }
}