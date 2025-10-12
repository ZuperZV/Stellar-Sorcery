package net.zuperz.stellar_sorcery.data;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CodexCategory {

    public final String id;
    public final ItemStack icon;
    public final List<CodexEntry> entries;
    public final int tier;

    public CodexCategory(String id, ItemStack icon, List<CodexEntry> entries, int tier) {
        this.id = id;
        this.icon = icon;
        this.entries = entries;
        this.tier = tier;
    }
}