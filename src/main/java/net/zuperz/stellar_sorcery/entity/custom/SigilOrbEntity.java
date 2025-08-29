package net.zuperz.stellar_sorcery.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.zuperz.stellar_sorcery.block.entity.custom.ArcForgeBlockEntity;
import net.zuperz.stellar_sorcery.component.CelestialData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import org.apache.commons.lang3.ObjectUtils;

public class SigilOrbEntity extends Mob {

    public SigilOrbEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    @Override
    protected void registerGoals() {}

    @Override
    public void tick() {
        super.tick();
    }


    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return super.mobInteract(player, hand);
        }

        if (!level().isClientSide) {
            BlockPos entityPos = this.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(
                    entityPos.offset(-3, -3, -3),
                    entityPos.offset(3, 3, 3))) {

                if (level().getBlockEntity(pos) instanceof ArcForgeBlockEntity arcForge && pos != null) {

                    if (!heldItem.isEmpty() && !arcForge.inventory.getStackInSlot(0).isEmpty()) {
                        arcForge.inventory.getStackInSlot(0).set(ModDataComponentTypes.CELESTIAL.get(), new CelestialData(heldItem));
                        player.getMainHandItem().shrink(1);
                        ItemStack stackOnPedestal = arcForge.inventory.extractItem(0, 1, false);
                        arcForge.inventory.insertItem(0, stackOnPedestal, false);
                        level().playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
                        level().sendBlockUpdated(arcForge.getBlockPos(), arcForge.getBlockState(), arcForge.getBlockState(), 3);

                        return InteractionResult.SUCCESS;

                    } else if (heldItem.isEmpty() && !arcForge.inventory.getStackInSlot(0).isEmpty()) {
                        CelestialData data = arcForge.inventory.getStackInSlot(0).get(ModDataComponentTypes.CELESTIAL.get());

                        if (data != null) {

                            player.setItemInHand(hand, data.getEmbeddedItem());
                            arcForge.inventory.getStackInSlot(0).remove(ModDataComponentTypes.CELESTIAL.get());
                            level().playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
                            level().sendBlockUpdated(arcForge.getBlockPos(), arcForge.getBlockState(), arcForge.getBlockState(), 3);

                            return InteractionResult.SUCCESS;
                        }
                    }

                    break;
                }
            }
        }

        return super.mobInteract(player, hand);
    }


    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entityIn) {}

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0D)
                .add(Attributes.GRAVITY, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void die(DamageSource cause) {
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }
}