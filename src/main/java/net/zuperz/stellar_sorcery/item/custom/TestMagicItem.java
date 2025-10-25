package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.spell.FireboltSpell;
import net.zuperz.stellar_sorcery.spell.ISpell;

public class TestMagicItem extends Item {

    private final ISpell spell = new FireboltSpell(); // midlertidigt hardcoded spell

    public TestMagicItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            spell.cast(level, player);
        }

        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
