package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.component.SpellData;
import net.zuperz.stellar_sorcery.data.SpellDataLoader;
import net.zuperz.stellar_sorcery.data.spell.*;
import net.zuperz.stellar_sorcery.item.ModItems;

import java.util.List;

public class SpellItem extends Item {

    public SpellItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        SpellData sd = SpellData.from(stack);

        if (sd == null) {

            sd = new SpellData(
                    "cone_small",
                    List.of("damage_basic", "ignite_rune"),
                    List.of("power_boost_1"),
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

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public void castSpell(Player player, SpellBlueprint spell) {

        AreaFile area = spell.area;
        List<RuneFile> runes = spell.runes;
        List<ModifierFile> mods = spell.modifiers;

        List<Entity> targets = AreaHandler.getTargets(player, area);

        SpellModifierEngine.applyModifiers(spell, runes, mods);

        for (RuneFile rune : runes) {
            RuneExecutor.executeRune(player, targets, rune);
        }

        //consumeMana(player, spell.manaCost);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }
}
