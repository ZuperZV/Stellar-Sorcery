package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.zuperz.stellar_sorcery.data.CodexDataLoader;
import net.zuperz.stellar_sorcery.data.CodexEntry;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.item.custom.decorator.CodexTooltip;
import net.zuperz.stellar_sorcery.screen.Helpers.RecipeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public class ItemStackCodexTooltipMixin {

    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)

    private void injectCodexTooltip(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack self = (ItemStack)(Object)this;

        if (findEntryForItem(self)) {
            player.getInventory().items.stream()
                    .filter(stack -> !stack.isEmpty() && stack.getItem() == ModItems.CODEX_ARCANUM.get())
                    .findFirst()
                    .ifPresent(codexStack -> cir.setReturnValue(Optional.of(new CodexTooltip(codexStack))));
        }
    }

    private static boolean findEntryForItem(ItemStack stack) {
        return getEntryForItem(stack).isPresent() || getEntryBySearchTag(stack).isPresent();
    }

    private static Optional<CodexEntry> getEntryForItem(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();

        for (CodexEntry entry : CodexDataLoader.getAllEntries()) {
            if (entry.icon == null || entry.icon.isEmpty()) continue;

            ItemStack iconStack = RecipeHelper.parseItem(entry.icon);
            if (ItemStack.isSameItem(stack, iconStack)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    private static Optional<CodexEntry> getEntryBySearchTag(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();

        String itemId = stack.getItem().getDescriptionId().toString();

        for (CodexEntry entry : CodexDataLoader.getAllEntries()) {
            if (entry.search_items == null) continue;

            for (String tag : entry.search_items) {
                if (tag.equals(itemId)) {
                    return Optional.of(entry);
                }
            }
        }

        return Optional.empty();
    }
}