package net.zuperz.stellar_sorcery.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CodexCategory {

    public final String id;
    public final String title;
    public final String iconId;
    public final ItemStack icon;
    public final List<CodexEntry> entries;
    public final List<Integer> tiers;
    public final int order;

    public CodexCategory(String id, ItemStack icon, List<CodexEntry> entries, List<Integer> tiers) {
        this(id, id, inferIconId(icon), icon, entries, tiers, Integer.MAX_VALUE);
    }

    public CodexCategory(String id, String title, String iconId, ItemStack icon, List<CodexEntry> entries, List<Integer> tiers, int order) {
        this.id = id;
        this.title = title;
        this.iconId = iconId;
        this.icon = icon;
        this.entries = entries;
        this.tiers = tiers;
        this.order = order;
    }

    public String getDisplayTitle() {
        if (title != null && !title.isBlank()) {
            return title;
        }
        return id;
    }

    private static String inferIconId(ItemStack icon) {
        if (icon == null || icon.isEmpty()) {
            return "minecraft:book";
        }

        ResourceLocation key = BuiltInRegistries.ITEM.getKey(icon.getItem());
        return key != null ? key.toString() : "minecraft:book";
    }
}
