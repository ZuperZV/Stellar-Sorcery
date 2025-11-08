package net.zuperz.stellar_sorcery.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.data.CodexDataLoader;
import net.zuperz.stellar_sorcery.data.CodexEntry;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.screen.CodexArcanumMenu;
import net.zuperz.stellar_sorcery.screen.CodexArcanumScreen;
import net.zuperz.stellar_sorcery.screen.Helpers.RecipeHelper;
import net.zuperz.stellar_sorcery.util.KeyBinding;
import org.joml.Matrix4f;
import org.joml.Vector4f;
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

        private static final ResourceLocation STAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/misc/star.png");

        private static final float STAR_SIZE = 50f;

        private static final float RENDER_DISTANCE = 500f;

        /*
        @SubscribeEvent
        public static void onRenderSkyStar(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            Camera camera = mc.gameRenderer.getMainCamera();
            Vec3 camPos = camera.getPosition();
            Vec3 lookVec = new Vec3(camera.getLookVector());
            Vec3 starWorldPos = camPos.add(lookVec.scale(RENDER_DISTANCE));

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.translate(starWorldPos.x - camPos.x,
                    starWorldPos.y - camPos.y,
                    starWorldPos.z - camPos.z);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, STAR_TEXTURE);

            Matrix4f matrix = poseStack.last().pose();

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS,
                    DefaultVertexFormat.POSITION_TEX_COLOR);

            float half = STAR_SIZE / 2f;

            builder.addVertex(matrix, -half, -half, 0f).setUv(0f, 1f).setColor(1f, 1f, 1f, 1f);
            builder.addVertex(matrix, -half,  half, 0f).setUv(0f, 0f).setColor(1f, 1f, 1f, 1f);
            builder.addVertex(matrix,  half,  half, 0f).setUv(1f, 0f).setColor(1f, 1f, 1f, 1f);
            builder.addVertex(matrix,  half, -half, 0f).setUv(1f, 1f).setColor(1f, 1f, 1f, 1f);

            MeshData mesh = builder.build();
            BufferUploader.drawWithShader(mesh);

            RenderSystem.disableBlend();
            poseStack.popPose();
        }
         */
    }
}