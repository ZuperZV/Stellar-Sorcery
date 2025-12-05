package net.zuperz.stellar_sorcery.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.zuperz.stellar_sorcery.capability.RecipesHelper.SoulCandleCommand;
import net.zuperz.stellar_sorcery.data.SigilDataLoader;
import net.zuperz.stellar_sorcery.item.custom.SigilItem;

import java.util.*;
import java.util.function.Consumer;

public class SigilData implements TooltipProvider {

    private final List<ItemStack> sigils;

    private final int capacity;

    public SigilData(List<ItemStack> sigils, int capacity) {
        this.sigils = new ArrayList<>(sigils);
        this.capacity = capacity;
    }

    public List<ItemStack> getSigils() {
        return Collections.unmodifiableList(sigils);
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean addSigil(ItemStack stack) {
        if (sigils.size() < capacity) {
            sigils.add(stack.copy());
            return true;
        }
        return false;
    }

    public ItemStack removeSigil(int index) {
        if (index >= 0 && index < sigils.size()) {
            return sigils.remove(index);
        }
        return ItemStack.EMPTY;
    }

    public boolean containsSigil(Item item) {
        return sigils.stream().anyMatch(s -> s.is(item));
    }

    public void clear() {
        sigils.clear();
    }

    public int getCount() {
        return sigils.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SigilData that)) return false;
        return capacity == that.capacity &&
                ItemStack.listMatches(this.sigils, that.sigils);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sigils, capacity);
    }

    public static SigilData fromIds(List<String> ids, int capacity) {
        List<ItemStack> stacks = new ArrayList<>();
        for (String idStr : ids) {
            ResourceLocation id = ResourceLocation.tryParse(idStr);
            if (id == null) continue;
            Item item = BuiltInRegistries.ITEM.get(id);
            stacks.add(new ItemStack(item));
        }
        return new SigilData(stacks, capacity);
    }

    public static SigilData from(ItemStack stack) {
        return stack.get(ModDataComponentTypes.SIGIL);
    }

    public static final Codec<SigilData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.listOf().optionalFieldOf("sigils", Collections.emptyList()).forGetter(SigilData::getSigils),
            Codec.INT.optionalFieldOf("capacity", 3).forGetter(SigilData::getCapacity)
    ).apply(instance, SigilData::new));


    @Override
    public void addToTooltip(Item.TooltipContext ctx, Consumer<Component> tooltip, TooltipFlag flag) {
        if (sigils.isEmpty())
            return;

        tooltip.accept(CommonComponents.EMPTY);
        tooltip.accept(
                Component.translatable("tooltip.stellar_sorcery.sigil_upgrade:")
                        .withStyle(ChatFormatting.GRAY)
        );

        System.out.println("Test1");

        for (ItemStack sigilStack : sigils) {

            String activeName = SigilItem.getActiveSigil(sigilStack);

            int color;
            try {
                color = SigilItem.getColor(sigilStack, 2);
            } catch (Exception e) {
                color = 0xFFFFFF;
            }

            tooltip.accept(
                    Component.literal("  ").append(
                            Component.translatable(activeName)
                                    .withColor(color)
                    )
            );
            System.out.println("Test2");

            List<MobEffectInstance> effects =
                    SigilDataLoader.getEffectsByName(activeName);

            for (MobEffectInstance e : effects) {

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

                tooltip.accept(
                        line.withStyle(e.getEffect().value().getCategory().getTooltipFormatting())
                );
                System.out.println("Test3");
            }

            SigilDataLoader.ShaderData shader = SigilDataLoader.getShaderByName(activeName);

            if (shader != null) {
                MutableComponent line =
                        Component.literal("" + shader.shaderId);

                if (shader.durationTicks > 20) {
                    line = line.append(" (")
                            .append(SigilItem.formatShaderDuration(shader, 1.0F, 20.0F))
                            .append(")");
                }

                tooltip.accept(line.withStyle(ChatFormatting.DARK_PURPLE));
                System.out.println("Test4");
            }

            List<SoulCandleCommand> commands =
                    SigilDataLoader.getCommandsByName(activeName);

            for (SoulCandleCommand cmd : commands) {
                tooltip.accept(
                        Component.translatable(cmd.getCommand())
                                .withStyle(ChatFormatting.GOLD)
                );
                System.out.println("Test5");
            }
        }
    }
}