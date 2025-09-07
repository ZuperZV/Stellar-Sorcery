package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.component.EssenceBottleData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.item.custom.decorator.EssenceBottleTooltip;
import net.zuperz.stellar_sorcery.item.custom.decorator.TextureColorHelper;
import net.zuperz.stellar_sorcery.shaders.post.DarkShaderRenderer;
import net.zuperz.stellar_sorcery.shaders.post.EssenceBottleItemShaderRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EssenceBottleItem extends Item {

    public EssenceBottleItem(Properties properties) {
        super(properties);
    }

    public @Nullable EssenceBottleData GetEssensBottle(ItemStack stack) {
        return stack.get(ModDataComponentTypes.ESSENCE_BOTTLE);
    }

    @Override
    public int getUseDuration(ItemStack p_41454_, LivingEntity p_344979_) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            EssenceBottleData data = stack.get(ModDataComponentTypes.ESSENCE_BOTTLE);
            if (data != null) {
                String key = makeKey(
                        data.getEmbeddedItem().getItem().builtInRegistryHolder().key().location().toString(),
                        data.getEmbeddedItem1().getItem().builtInRegistryHolder().key().location().toString(),
                        data.getEmbeddedItem2().getItem().builtInRegistryHolder().key().location().toString()
                );

                List<MobEffectInstance> effects = EssenceDataLoader.getEffects(key);
                for (MobEffectInstance effect : effects) {
                    entity.addEffect(new MobEffectInstance(effect));
                }
            }
        } else {
            EssenceBottleData data = stack.get(ModDataComponentTypes.ESSENCE_BOTTLE);
            if (data != null) {
                String key = makeKey(
                        data.getEmbeddedItem().getItem().builtInRegistryHolder().key().location().toString(),
                        data.getEmbeddedItem1().getItem().builtInRegistryHolder().key().location().toString(),
                        data.getEmbeddedItem2().getItem().builtInRegistryHolder().key().location().toString()
                );

                EssenceDataLoader.ShaderData shader = EssenceDataLoader.getShader(key);
                if (shader != null) {
                    EssenceBottleItemShaderRenderer.enableShader(shader.shaderId, shader.durationTicks);
                }
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public Component getName(ItemStack stack) {
        var bottleData = GetEssensBottle(stack);

        if (bottleData != null &&
                bottleData.getEmbeddedItem() != null &&
                bottleData.getEmbeddedItem1() != null &&
                bottleData.getEmbeddedItem2() != null) {

            String id0 = bottleData.getEmbeddedItem().getItem().builtInRegistryHolder().key().location().toString();
            String id1 = bottleData.getEmbeddedItem1().getItem().builtInRegistryHolder().key().location().toString();
            String id2 = bottleData.getEmbeddedItem2().getItem().builtInRegistryHolder().key().location().toString();

            String key = makeKey(id0, id1, id2);
            String customTranslationKey = EssenceDataLoader.getName(key);

            if (customTranslationKey != null) {
                return Component.translatable(customTranslationKey);
            }
            else {
                return Component.translatable("item.stellar_sorcery.bottle_essence_of")
                        .append(" ")
                        .append(Component.translatable(bottleData.getEmbeddedItem().getItem().getDescriptionId()))
                        .append(Component.translatable("tooltip.stellar_sorcery.infused"))
                        .append(Component.translatable(bottleData.getEmbeddedItem1().getItem().getDescriptionId()))
                        .append(Component.translatable("tooltip.stellar_sorcery.with"))
                        .append(Component.translatable(bottleData.getEmbeddedItem2().getItem().getDescriptionId()));
            }
        } else {
            return Component.translatable("item.stellar_sorcery.bottle_essence_of");
        }
    }

    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 1) return 0xFFFFFFFF;

        EssenceBottleData data = stack.get(ModDataComponentTypes.ESSENCE_BOTTLE);
        if (data == null) return 0xFFFFFFFF;

        List<ItemStack> items = List.of(data.getEmbeddedItem(), data.getEmbeddedItem1(), data.getEmbeddedItem2());

        int r = 0, g = 0, b = 0, count = 0;

        for (ItemStack s : items) {
            if (!s.isEmpty()) {
                int color = TextureColorHelper.getAverageColor(s);
                r += (color >> 16) & 0xFF;
                g += (color >> 8) & 0xFF;
                b += color & 0xFF;
                count++;
            }
        }

        if (count == 0) return 0xFFFFFFFF;

        r /= count;
        g /= count;
        b /= count;

        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    private String makeKey(String... ids) {
        List<String> sorted = Arrays.asList(ids);
        Collections.sort(sorted);
        return String.join(",", sorted);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        EssenceBottleData data = stack.get(ModDataComponentTypes.ESSENCE_BOTTLE);
        if (data != null) {
            return Optional.of(new EssenceBottleTooltip(
                    data.getEmbeddedItem(),
                    data.getEmbeddedItem1(),
                    data.getEmbeddedItem2()
            ));
        }
        return Optional.empty();
    }

    @Override
    public Component getDescription() {
        return this.getName(ItemStack.EMPTY);
    }

    @Override
    public String getDescriptionId(ItemStack pStack) {
        return "item.stellar_sorcery.bottle_essence_of";
    }
}
