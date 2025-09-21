package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.component.PlayerData;
import net.zuperz.stellar_sorcery.item.ModItems;

public class RitualDaggerItem extends SwordItem {
    public RitualDaggerItem(Tier tier, Item.Properties properties) {
        super(tier, properties.component(DataComponents.TOOL, createToolProperties()));
    }

    @Override
    public InteractionResultHolder<ItemStack> use (Level world, Player player, InteractionHand hand){
        ItemStack daggerStack = player.getItemInHand(hand);

        player.hurt(
                new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC)),
                2
        );

        PlayerData data = new PlayerData(player.getUUID(), player.getGameProfile().getName());

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);

            if (invStack.getItem() == ModItems.BLOOD_VIAL.get()) {
                if (invStack.get(ModDataComponentTypes.PLAYER_DATA) == null) {
                    invStack.set(ModDataComponentTypes.PLAYER_DATA, data);
                    break;
                }
            }
        }

        EquipmentSlot slot = (hand == InteractionHand.MAIN_HAND)
                ? EquipmentSlot.MAINHAND
                : EquipmentSlot.OFFHAND;
        daggerStack.hurtAndBreak(1, player, slot);

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);

        return InteractionResultHolder.sidedSuccess(daggerStack, world.isClientSide());
    }
}