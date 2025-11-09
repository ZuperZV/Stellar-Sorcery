package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.component.SigilNameData;
import net.zuperz.stellar_sorcery.data.SigilDataLoader;
import net.zuperz.stellar_sorcery.shaders.post.EssenceBottleItemShaderRenderer;

import java.util.Random;

public class SigilItem extends Item {

    public SigilItem(Properties properties) {
        super(properties);
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


    public static void setActiveSigil(ItemStack stack, String sigilName) {
        stack.set(ModDataComponentTypes.SIGIL_NAME.get(), new SigilNameData(sigilName));
    }

    public static String getActiveSigil(ItemStack stack) {
        SigilNameData data = stack.get(ModDataComponentTypes.SIGIL_NAME.get());
        return data != null ? data.name() : "";
    }
}