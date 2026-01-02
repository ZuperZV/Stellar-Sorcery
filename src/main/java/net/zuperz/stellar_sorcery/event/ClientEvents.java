package net.zuperz.stellar_sorcery.event;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.zuperz.stellar_sorcery.capability.RecipesHelper.SoulCandleCommand;
import net.zuperz.stellar_sorcery.planet.PlanetRenderer;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.component.SigilData;
import net.zuperz.stellar_sorcery.data.CodexDataLoader;
import net.zuperz.stellar_sorcery.data.CodexEntry;
import net.zuperz.stellar_sorcery.data.SigilDataLoader;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.item.custom.SigilItem;
import net.zuperz.stellar_sorcery.screen.CodexArcanumMenu;
import net.zuperz.stellar_sorcery.screen.CodexArcanumScreen;
import net.zuperz.stellar_sorcery.screen.Helpers.RecipeHelper;
import net.zuperz.stellar_sorcery.util.KeyBinding;

import java.util.List;
import java.util.Optional;

import static net.zuperz.stellar_sorcery.StellarSorcery.MOD_ID;

@OnlyIn(Dist.CLIENT)
public class ClientEvents {

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {

            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
                return;
            }

            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;

            float partialTick =
                    event.getPartialTick()
                            .getGameTimeDeltaPartialTick(false);

            PlanetRenderer.render(
                    event.getPoseStack(),
                    level,
                    partialTick
            );
        }

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

        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();

            SigilData sigilData = stack.get(ModDataComponentTypes.SIGIL);
            if (sigilData == null || sigilData.getSigils().isEmpty()) {
                return;
            }

            List<Component> tooltip = event.getToolTip();

            tooltip.add(CommonComponents.EMPTY);

            tooltip.add(
                    Component.translatable("tooltip.stellar_sorcery.sigil_upgrade:")
                            .withStyle(ChatFormatting.GRAY)
            );

            for (ItemStack sigilStack : sigilData.getSigils()) {

                String name = SigilItem.getActiveSigil(sigilStack);

                int color;
                try {
                    color = SigilItem.getColor(sigilStack, 2);
                } catch (Exception e) {
                    color = 0xFFFFFF;
                }

                MutableComponent sigilLine = Component.literal("  ")
                        .append(Component.translatable("item.stellar_sorcery." + name).withColor(color));

                tooltip.add(sigilLine);

                for (MobEffectInstance effect : SigilDataLoader.getEffectsByName(name)) {

                    MutableComponent line = Component.literal("  -").append(Component.translatable(effect.getDescriptionId()));

                    if (effect.getAmplifier() > 0) {
                        line.append("  ")
                                .append(
                                        Component.translatable("potion.potency." + effect.getAmplifier())
                                );
                    }

                    if (effect.getDuration() > 20) {
                        line.append(" (")
                                .append(MobEffectUtil.formatDuration(effect, 1.0F, 20.0F))
                                .append(")");
                    }

                    tooltip.add(line.withStyle(effect.getEffect().value().getCategory().getTooltipFormatting()));
                }

                SigilDataLoader.ShaderData shader = SigilDataLoader.getShaderByName(name);

                if (shader != null) {

                    MutableComponent shaderText = Component.literal("  -").append(Component.translatable(String.valueOf(shader.shaderId)));

                    if (shader.durationTicks > 20) {
                        shaderText.append(" (")
                                .append(SigilItem.formatShaderDuration(shader, 1.0F, 20.0F))
                                .append(")");
                    }

                    tooltip.add(shaderText.withStyle(ChatFormatting.DARK_PURPLE));
                }

                for (SoulCandleCommand cmd : SigilDataLoader.getCommandsByName(name)) {
                    tooltip.add(
                            Component.literal("  -").withStyle(ChatFormatting.GOLD).append(Component.translatable(cmd.getCommand())
                                    .withStyle(ChatFormatting.GOLD))
                    );
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