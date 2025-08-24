package net.zuperz.stellar_sorcery.api.jei;

import mezz.jei.api.gui.drawable.IDrawableAnimated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.item.Item;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/*
 *  MIT License
 *  Copyright (c) 2022 Kaupenjoe
 *
 *  This code is licensed under the "MIT License"
 *  https://github.com/Kaupenjoe/Resource-Slimes/blob/master/LICENSE
 *
 *  EntityDrawable Class by Kaupenjoe:
 *  https://github.com/Kaupenjoe/Resource-Slimes/blob/master/src/main/java/net/kaupenjoe/resourceslimes/integration/EntityDrawable.java
 *
 *  Kaupenjoe Has it from
 *
 *  EntityWidget Class by DaRealTurtyWurty:
 *  https://github.com/DaRealTurtyWurty/TurtyLib/blob/main/src/main/java/io/github/darealturtywurty/turtylib/client/ui/components/EntityWidget.javaz
 *
 *  Modified by: ZuperZ
 */
public class EntityDrawable implements IDrawableAnimated {
    private LivingEntity livingEntity;
    private int size;
    private int width;
    private int height;

    public EntityDrawable(int height, int width, EntityType<? extends LivingEntity> entityType, int size) {
        this(entityType, size, width, height, null);
    }

    private EntityDrawable(EntityType<? extends LivingEntity> entityType, int size, int width, int height, Item resourceItem) {
        this.size = size;
        this.width = width;
        this.height = height;

        Entity entity = entityType.create(Minecraft.getInstance().level);

        if (entity instanceof LivingEntity living) {
            this.livingEntity = living;
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        if (livingEntity != null) {
            livingEntity.setYHeadRot(0);
            livingEntity.setPos(0, 0, 0);

            InventoryScreen.renderEntityInInventory(
                    guiGraphics,
                    xOffset, yOffset,
                    size,
                    new Vector3f(0, 0, 0),
                    new Quaternionf().rotationYXZ((float) Math.PI / 1.3f, 0, (float) Math.PI),
                    null,
                    livingEntity
            );
        }
    }
}