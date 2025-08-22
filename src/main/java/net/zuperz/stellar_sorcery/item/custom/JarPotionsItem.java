package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.zuperz.stellar_sorcery.item.ModItems;

import java.util.List;

public class JarPotionsItem extends ThrowablePotionItem {
    private final Holder<Potion> defaultEffect;

    public JarPotionsItem(Holder<Potion> effect) {
        super(new Item.Properties());
        this.defaultEffect = effect;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        defaultEffect(player.getMainHandItem());

        player.startUsingItem(context.getHand());

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        defaultEffect(player.getMainHandItem());

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SPLASH_POTION_THROW,
                SoundSource.PLAYERS,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );

        return super.use(level, player, hand);
    }

    private void defaultEffect(ItemStack stack) {
        if (this.defaultEffect != null) {
            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(this.defaultEffect));
        } else {
            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
        }
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        defaultEffect(stack);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        Player player = entity instanceof Player ? (Player) entity : null;

        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }

        if (!level.isClientSide) {
            PotionContents potioncontents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            potioncontents.forEachEffect(effect -> {
                if (effect.getEffect().value().isInstantenous()) {
                    effect.getEffect().value().applyInstantenousEffect(player, player, entity, effect.getAmplifier(), 1.0);
                } else {
                    entity.addEffect(effect);
                }
            });
        }

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
            stack.consume(1, player);
            if (!player.hasInfiniteMaterials()) {
                if (stack.isEmpty()) {
                    return new ItemStack(ModItems.CLAY_JAR.get());
                }
                player.getInventory().add(new ItemStack(ModItems.CLAY_JAR.get()));
            }
        }

        entity.gameEvent(GameEvent.DRINK);
        return stack;
    }

    @Override
    public String getDescriptionId() {
        return Util.makeDescriptionId("item", BuiltInRegistries.ITEM.getKey(this));
    }

    @Override
    public void appendHoverText(ItemStack p_42988_, Item.TooltipContext p_339608_, List<Component> p_42990_, TooltipFlag p_42991_) {
        PotionContents potioncontents = p_42988_.get(DataComponents.POTION_CONTENTS);
        if (potioncontents != null) {
            potioncontents.addPotionTooltip(p_42990_::add, 1.0F, p_339608_.tickRate());
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(this);
        String itemId = "item." + key.getNamespace() + "." + key.getPath();
        return Component.translatable(itemId);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int p_41407_, boolean p_41408_) {
        super.inventoryTick(stack, level, entity, p_41407_, p_41408_);
        defaultEffect(stack);
    }
}
