package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.component.EssenceBottleData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.component.SpellData;
import net.zuperz.stellar_sorcery.data.EssenceDataLoader;
import net.zuperz.stellar_sorcery.data.SpellDataLoader;
import net.zuperz.stellar_sorcery.data.spell.*;
import net.zuperz.stellar_sorcery.item.ModItems;

import java.util.ArrayList;
import java.util.List;

public class SpellItem extends Item {

    public SpellItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
            Player player = entity instanceof Player ? (Player) entity : null;

        SpellData sd = SpellData.from(stack);

        if (sd == null) {

            AreaFile area = SpellRegistry.getRandomArea();

            List<String> runes = new ArrayList<>();
            int runeCount = 1 + level.random.nextInt(3);
            for (int i = 0; i < runeCount; i++) {
                RuneFile rf = SpellRegistry.getRandomRune();
                if (rf != null) runes.add(rf.id);
            }

            List<String> mods = new ArrayList<>();
            int modCount = level.random.nextInt(3);
            for (int i = 0; i < modCount; i++) {
                ModifierFile mf = SpellRegistry.getRandomModifier();
                if (mf != null) mods.add(mf.id);
            }

            sd = new SpellData(
                    area != null ? area.id : "default_area",
                    runes,
                    mods,
                    15
            );

            stack.set(ModDataComponentTypes.SPELL_DATA, sd);
        }

        SpellBlueprint spell = new SpellBlueprint(
                "custom_spell",
                SpellRegistry.getArea(sd.getArea()),
                sd.getRunes().stream().map(SpellRegistry::getRune).toList(),
                sd.getModifiers().stream().map(SpellRegistry::getModifier).toList(),
                sd.getHealthCost()
        );

        castSpell(player, spell);

        return stack;
    }

    public void castSpell(Player player, SpellBlueprint spell) {
        Level level = player.level();

        AreaFile area = spell.area;
        List<RuneFile> runes = spell.runes;
        List<ModifierFile> mods = spell.modifiers;

        List<Entity> targets = AreaHandler.getTargets(player, area);
        BlockPos areaPos = AreaHandler.getAreaPos(player, area);

        level.playSound(
                null,
                areaPos,
                SoundEvents.DECORATED_POT_INSERT,
                SoundSource.BLOCKS,
                1.0F,
                0.7F + 0.5F
        );

        if (level instanceof ServerLevel serverLevel) {
            if (area.normal_particles) {
                List<BlockPos> blocks = AreaHandler.getAllBlocksInArea(areaPos, area.range);

                for (BlockPos pos : blocks) {
                    serverLevel.sendParticles(
                            ParticleTypes.DUST_PLUME,
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            1,
                            0.0,
                            0.0,
                            0.0,
                            0.0
                    );
                }
            }
        }

        SpellModifierEngine.applyModifiers(spell, runes, mods);

        for (RuneFile rune : runes) {
            RuneExecutor.executeRune(player, targets, rune);
        }

        //consumeMana(player, spell.manaCost);
    }

    @Override
    public int getUseDuration(ItemStack p_41454_, LivingEntity p_344979_) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag flag) {
        SpellData data = stack.get(ModDataComponentTypes.SPELL_DATA);
        if (data != null) {

            if (!data.getArea().isEmpty()) {
                tooltip.add(Component.literal("Area: ").withStyle(ChatFormatting.GOLD));

                tooltip.add(Component.literal(" ")
                        .append(Component.translatable(data.getArea()).withStyle(ChatFormatting.AQUA)));
            }

            if (!data.getRunes().isEmpty()) {
                tooltip.add(Component.literal("Runes:").withStyle(ChatFormatting.BLUE));

                for (String rune : data.getRunes()) {
                    tooltip.add(Component.literal(" ")
                            .append(Component.translatable(rune).withStyle(ChatFormatting.AQUA)));
                }
            }

            if (!data.getModifiers().isEmpty()) {
                tooltip.add(Component.literal("Modifiers:").withStyle(ChatFormatting.LIGHT_PURPLE));

                for (String mod : data.getModifiers()) {
                    tooltip.add(Component.literal(" ")
                            .append(Component.translatable(mod).withStyle(ChatFormatting.AQUA)));
                }
            }

            if (data.getHealthCost() != 0) {
                tooltip.add(Component.literal("Sacrifice: ").withStyle(ChatFormatting.DARK_RED));

                tooltip.add(Component.literal(" ")
                        .append(Component.translatable(String.valueOf(data.getHealthCost())).withStyle(ChatFormatting.RED)));
            }

            tooltip.add(Component.empty());
        }
    }
}
