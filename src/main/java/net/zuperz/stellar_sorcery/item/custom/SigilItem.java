package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.capability.RecipesHelper.SoulCandleCommand;
import net.zuperz.stellar_sorcery.component.*;
import net.zuperz.stellar_sorcery.data.EssenceDataLoader;
import net.zuperz.stellar_sorcery.data.SigilDataLoader;
import net.zuperz.stellar_sorcery.item.custom.decorator.TextureColorHelper;
import net.zuperz.stellar_sorcery.shaders.post.EssenceBottleItemShaderRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SigilItem extends Item {

    public SigilItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        String activeSigilName = getActiveSigil(stack);

        if (activeSigilName.isEmpty()) {
            return Component.translatable("item.stellar_sorcery.sigil");
        }

        return Component.translatable("item.stellar_sorcery.sigil_of")
                .append(" ")
                .append(Component.translatable("item.stellar_sorcery." + activeSigilName));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (stack.get(ModDataComponentTypes.SIGIL_NAME.get()) == null) {

                setActiveSigil(stack, SigilDataLoader.getRandomName(new Random()));

                player.displayClientMessage(Component.literal("Got sigil: " + getActiveSigil(stack)), true);
            } else {

                String activeSigilName = getActiveSigil(stack);

                SigilDataLoader.SigilDefinition sigil = SigilDataLoader.getByName(activeSigilName);
                if (sigil == null) {
                    player.displayClientMessage(Component.literal("Unknown sigil: " + activeSigilName), true);
                    return InteractionResultHolder.fail(stack);
                }

                for (MobEffectInstance effect : sigil.effects()) {
                    player.addEffect(new MobEffectInstance(effect));
                }

                if (sigil.shader() != null) {
                    EssenceBottleItemShaderRenderer.enableShader(sigil.shader().shaderId, sigil.shader().durationTicks);
                }

                player.displayClientMessage(Component.literal("Activated sigil: " + sigil.name()), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 2) return 0xFFFFFFFF;

        String sigilName = getActiveSigil(stack);
        if (sigilName.isEmpty()) return 0xFFFFFFFF;

        String colorString = SigilDataLoader.getColorByName(sigilName);
        if (colorString == null || colorString.isEmpty()) return 0xFFFFFFFF;

        colorString = colorString.replace("#", "");

        int colorValue;
        try {
            colorValue = (int) Long.parseLong(colorString, 16);
            if (colorString.length() <= 6) colorValue |= 0xFF000000;
        } catch (NumberFormatException e) {
            colorValue = 0xFFFFFFFF;
        }

        return colorValue;
    }

    public static void setActiveSigil(ItemStack stack, String sigilName) {
        stack.set(ModDataComponentTypes.SIGIL_NAME.get(), new SigilNameData(sigilName));
    }

    public static String getActiveSigil(ItemStack stack) {
        SigilNameData data = stack.get(ModDataComponentTypes.SIGIL_NAME.get());
        return data != null ? data.name() : "";
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        SigilNameData nameData = pStack.get(ModDataComponentTypes.SIGIL_NAME.get());

        List<MobEffectInstance> effects = SigilDataLoader.getEffectsByName(nameData.name());
        List<MobEffectInstance> toolTipEffects = new ArrayList<>();

        if (!effects.isEmpty()) {
            toolTipEffects.addAll(effects);
        }

        if (!toolTipEffects.isEmpty()) {
            for (MobEffectInstance e : toolTipEffects) {
                MutableComponent line = Component.translatable(e.getDescriptionId());

                if (e.getAmplifier() > 0) {
                    line = line.append(" ")
                            .append(Component.translatable("potion.potency." + e.getAmplifier()));
                }

                if (e.getDuration() > 20) {
                    line = line.append(" (")
                            .append(MobEffectUtil.formatDuration(e, 1.0F, 20.0F))
                            .append(")");
                }

                pTooltipComponents.add(line.withStyle(e.getEffect().value().getCategory().getTooltipFormatting()));
            }
        }

        SigilDataLoader.ShaderData shader = SigilDataLoader.getShaderByName(nameData.name());

        if (shader != null) {
            MutableComponent line = Component.translatable(String.valueOf(shader.shaderId));


            if (shader.durationTicks > 20) {
                line = line.append(" (")
                        .append(formatShaderDuration(shader, 1.0F, 20.0F))
                        .append(")");
            }

            pTooltipComponents.add(line.withStyle(ChatFormatting.DARK_PURPLE));
        }

        List<SoulCandleCommand> commands = SigilDataLoader.getCommandsByName(nameData.name());

        if (!commands.isEmpty()) {
            for (SoulCandleCommand cmd : commands) {
                MutableComponent line = Component.translatable(cmd.getCommand());

                pTooltipComponents.add(line.withStyle(ChatFormatting.GOLD));
            }
        }




        SigilData data = pStack.get(ModDataComponentTypes.SIGIL.get());
        if (data != null) {

            pTooltipComponents.add(CommonComponents.EMPTY);
            pTooltipComponents.add(Component.translatable("tooltip.stellar_sorcery.sigil_upgrade:").withStyle(style -> style.withColor(ChatFormatting.GRAY)));


            for (int i = 0; i < data.getSigils().size(); i++) {
                ItemStack stack = data.getSigils().get(i);
                String activeSigilName = getActiveSigil(stack);

                int color;
                try {
                    color = getColor(stack, 2);
                } catch (Exception e) {
                    color = 0xFFFFFF;
                }

                int finalColor = color;
                pTooltipComponents.add(Component.translatable(activeSigilName).withStyle(style -> style.withColor(finalColor)));
            }
        }
        super.appendHoverText(pStack, (TooltipContext) pContext, pTooltipComponents, pTooltipFlag);
    }

    public static Component formatShaderDuration(SigilDataLoader.ShaderData shader, float p_268280_, float p_314720_) {
        if (shader.durationTicks == -1) {
            return Component.translatable("effect.duration.infinite");
        } else {
            int i = Mth.floor((float)shader.durationTicks * p_268280_);
            return Component.literal(StringUtil.formatTickDuration(i, p_314720_));
        }
    }

}