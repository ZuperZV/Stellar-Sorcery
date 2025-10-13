package net.zuperz.stellar_sorcery.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.data.CodexDataLoader;
import net.zuperz.stellar_sorcery.data.CodexEntry;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.screen.CodexArcanumMenu;
import net.zuperz.stellar_sorcery.screen.CodexArcanumScreen;
import net.zuperz.stellar_sorcery.screen.Helpers.RecipeHelper;
import net.zuperz.stellar_sorcery.util.KeyBinding;
import org.slf4j.Logger;

import java.util.Optional;

import static net.zuperz.stellar_sorcery.StellarSorcery.MOD_ID;

@OnlyIn(Dist.CLIENT)
public class CodexClientEvents {

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        @SubscribeEvent
        public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (KeyBinding.OPEN_BOOK.matches(event.getKeyCode(), event.getScanCode())) {
                if (mc.player != null) {

                    if (!(mc.screen instanceof AbstractContainerScreen<?> containerScreen)) {
                        return;
                    }

                    ItemStack stack = mc.player.getMainHandItem();

                    if (stack.isEmpty()) stack = mc.player.getOffhandItem();

                    if (stack.isEmpty() || !stack.is(ModItems.CODEX_ARCANUM.get())) {
                        stack = mc.player.getInventory().items.stream()
                                .filter(stackItem -> !stackItem.isEmpty() && stackItem.getItem() == ModItems.CODEX_ARCANUM.get())
                                .findFirst()
                                .orElse(ItemStack.EMPTY);
                    }

                    if (!stack.is(ModItems.CODEX_ARCANUM.get())) return;

                    ItemStack hovered = ItemStack.EMPTY;
                    var slot = containerScreen.getSlotUnderMouse();
                    if (slot != null && !slot.getItem().isEmpty()) {
                        hovered = slot.getItem();
                    } else {
                        return;
                    }


                    ItemStack finalHovered = hovered;
                    Optional<CodexEntry> entryOpt = CodexDataLoader.getAllEntries().stream()
                            .filter(e -> {
                                if (e.icon == null || e.icon.isEmpty()) return false;
                                ItemStack iconStack = RecipeHelper.parseItem(e.icon);
                                return ItemStack.isSameItemSameComponents(iconStack, finalHovered);
                            })
                            .findFirst();

                    if (entryOpt.isEmpty()) {
                        return;
                    }

                    CodexEntry entry = entryOpt.get();
                    CodexArcanumMenu dummyMenu = new CodexArcanumMenu(0, mc.player.getInventory().player);
                    CodexArcanumScreen screen = new CodexArcanumScreen(
                            dummyMenu,
                            mc.player.getInventory(),
                            Component.literal("Codex Arcanum")
                    );

                    screen.selectedEntry = entry;
                    screen.selectedPage = 0;
                    screen.categories = CodexDataLoader.getAllCategories();

                    screen.selectedCategory = screen.getCategoryForEntry(entry);
                    screen.isInCategoryView = false;

                    mc.setScreen(screen);
                }
            }
        }
    }
}