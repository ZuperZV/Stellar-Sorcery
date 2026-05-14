package net.zuperz.stellar_sorcery.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;

import java.util.function.Supplier;

public class PotionFluidType extends BaseFluidType {

    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;
    private final ResourceLocation overlayTexture;

    public PotionFluidType(
            ResourceLocation stillTexture,
            ResourceLocation flowingTexture,
            ResourceLocation overlayTexture,
            Properties properties
    ) {
        super(
                stillTexture,
                flowingTexture,
                overlayTexture,
                0xFFFFFFFF,
                new org.joml.Vector3f(0.3f, 0.6f, 1.0f),
                properties
        );

        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.overlayTexture = overlayTexture;
    }

    @Override
    public IClientFluidTypeExtensions getClientFluidTypeExtensions() {
        return new IClientFluidTypeExtensions() {

            @Override
            public int getTintColor(FluidStack stack) {
                PotionContents contents =
                        stack.get(ModDataComponentTypes.POTION_CONTENTS);

                if (contents == null || contents == PotionContents.EMPTY) {
                    return 0xFFFFFFFF;
                }

                return contents.getColor();
            }

            @Override
            public ResourceLocation getStillTexture() {
                return stillTexture;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return flowingTexture;
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return overlayTexture;
            }
        };
    }
}