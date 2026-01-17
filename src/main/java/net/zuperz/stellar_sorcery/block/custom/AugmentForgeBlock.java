package net.zuperz.stellar_sorcery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.zuperz.stellar_sorcery.block.entity.custom.AugmentForgeBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.AugmentForgeBlockEntity;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.component.SigilData;
import net.zuperz.stellar_sorcery.component.SigilNameData;
import net.zuperz.stellar_sorcery.data.SigilDataLoader;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class AugmentForgeBlock extends BaseEntityBlock {

public static final MapCodec<AstralNexusBlock> CODEC = simpleCodec(AstralNexusBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(
            box(13, 12, 1, 15, 13, 3),
            box(1, 12, 1, 3, 13, 3),
            box(1, 12, 13, 3, 13, 15),
            box(13, 12, 13, 15, 13, 15),
            box(1, 0, 1, 15, 2, 15),
            box(1, 9, 1, 15, 12, 15),
            box(3, 2, 3, 13, 9, 13),
            box(4.999, 2.5, 1.5, 10.999, 8.5, 4.5),
            box(11.5, 2.5, 4.999, 14.5, 8.5, 10.999),
            box(5.001, 2.5, 11.5, 11.001, 8.5, 14.5),
            box(1.5, 2.5, 5.001, 4.5, 8.5, 11.001),
            box(12, 8, 2, 14, 9, 4),
            box(12, 2, 12, 14, 3, 14),
            box(2, 2, 12, 4, 3, 14),
            box(2, 2, 2, 4, 3, 4),
            box(12, 2, 2, 14, 3, 4),
            box(2, 8, 2, 4, 9, 4),
            box(2, 8, 12, 4, 9, 14),
            box(12, 8, 12, 14, 9, 14)
    );

    public AugmentForgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AugmentForgeBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState();
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos,
                            BlockState pNewState, boolean pMovedByPiston) {
        if(pState.getBlock() != pNewState.getBlock()) {
            if(pLevel.getBlockEntity(pPos) instanceof AugmentForgeBlockEntity AugmentForgeBlockEntity) {
                AugmentForgeBlockEntity.drops();
                pLevel.updateNeighbourForOutputSignal(pPos, this);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos,
                                              Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if (!pLevel.isClientSide) {
            if (isLookingAtFilterPlane(pPos, pPlayer) &&
                    pLevel.getBlockEntity(pPos) instanceof AugmentForgeBlockEntity forgeBE) {

                ItemStack stack = forgeBE.getInputItems().getStackInSlot(0);

                if (!stack.isEmpty()) {
                    SigilData data = stack.get(ModDataComponentTypes.SIGIL.get());
                    if (data == null) {
                        data = new SigilData(new ArrayList<>(), 1);
                        stack.set(ModDataComponentTypes.SIGIL.get(), data);
                    }

                    if (!data.getSigils().isEmpty()) {
                        pPlayer.displayClientMessage(Component.literal("TEST - Har allerede sigils!"), true);

                        boolean addedToInventory = false;

                        for (int i = 0; i < pPlayer.getInventory().items.size(); i++) {
                            ItemStack playerStack = pPlayer.getInventory().items.get(i);

                            if (!playerStack.isEmpty()
                                    && ItemStack.isSameItem(playerStack, data.getSigils().get(0))
                                    && playerStack.getCount() < playerStack.getMaxStackSize()) {

                                playerStack.grow(1);
                                addedToInventory = true;
                                break;
                            }
                        }

                        if (!addedToInventory && pStack.isEmpty()) {
                            pPlayer.setItemInHand(InteractionHand.MAIN_HAND, data.getSigils().get(0));
                            addedToInventory = true;
                        }

                        if (addedToInventory) {
                            data.removeSigil(0);
                            pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
                        }

                        forgeBE.setChanged();

                        pLevel.sendBlockUpdated(pPos, pState, pState, Block.UPDATE_ALL);

                        return ItemInteractionResult.SUCCESS;
                    } else if (!pStack.isEmpty()) {
                        ItemStack newSigil = pStack.copy();

                        SigilNameData nameData = newSigil.get(ModDataComponentTypes.SIGIL_NAME.get());
                        String sigilArmorType = SigilDataLoader.getArmorByName(nameData.name());
                        String stackArmorType = "";

                        if (isArmor(stack.getItem(), ArmorItem.Type.HELMET)) {
                            stackArmorType = "helmet";
                        } else if (isArmor(stack.getItem(), ArmorItem.Type.CHESTPLATE)) {
                            stackArmorType = "chestplate";
                        } else if (isArmor(stack.getItem(), ArmorItem.Type.LEGGINGS)) {
                            stackArmorType = "leggings";
                        } else if (isArmor(stack.getItem(), ArmorItem.Type.BOOTS)) {
                            stackArmorType = "boots";
                        }

                        if (!stackArmorType.equalsIgnoreCase(sigilArmorType)) {
                            pPlayer.displayClientMessage(Component.literal("Sigil passer ikke til denne armor type!"), true);
                            System.out.println("stackArmorType: " + stackArmorType);
                            System.out.println("sigilArmorType: " + sigilArmorType);
                            return ItemInteractionResult.FAIL;
                        }

                        pStack.shrink(1);

                        data.addSigil(newSigil);
                        stack.set(ModDataComponentTypes.SIGIL.get(), data);
                        forgeBE.getInputItems().setStackInSlot(0, stack);

                        forgeBE.setChanged();
                        pLevel.sendBlockUpdated(pPos, pState, pState, Block.UPDATE_ALL);

                        pPlayer.displayClientMessage(Component.literal("Tilføjede sigil: " +
                                newSigil.getHoverName().getString()), true);

                        return ItemInteractionResult.SUCCESS;
                    }
                }
            } else if (pLevel.getBlockEntity(pPos) instanceof AugmentForgeBlockEntity nexus) {

                if (nexus.inventory.getStackInSlot(0).isEmpty() && !pStack.isEmpty()) {
                    nexus.inventory.insertItem(0, pStack.copy(), false);
                    pStack.shrink(1);
                    pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
                    return ItemInteractionResult.SUCCESS;
                }

                else if (pStack.isEmpty() || !pStack.isEmpty() && !nexus.inventory.getStackInSlot(0).isEmpty()) {
                    ItemStack extracted = nexus.inventory.extractItem(0, 1, true);

                    if (!extracted.isEmpty()) {
                        boolean addedToInventory = false;

                        for (int i = 0; i < pPlayer.getInventory().items.size(); i++) {
                            ItemStack playerStack = pPlayer.getInventory().items.get(i);

                            if (!playerStack.isEmpty()
                                    && ItemStack.isSameItem(playerStack, extracted)
                                    && playerStack.getCount() < playerStack.getMaxStackSize()) {

                                playerStack.grow(1);
                                addedToInventory = true;
                                break;
                            }
                        }

                        if (!addedToInventory && pStack.isEmpty()) {
                            pPlayer.setItemInHand(InteractionHand.MAIN_HAND, extracted);
                            addedToInventory = true;
                        }

                        if (addedToInventory) {
                            nexus.clearContents();
                            nexus.inventory.extractItem(0, 1, false);
                            pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
                        }
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
        }

        return ItemInteractionResult.SUCCESS;
    }

    private boolean isLookingAtFilterPlane(BlockPos pos, Player player) {
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 lookVec = player.getLookAngle();
        Vec3 end = start.add(lookVec.scale(5.0));

        AABB filterArea = new AABB(
                pos.getX() + 0.3, pos.getY() + 0.3, pos.getZ() + 0.3,
                pos.getX() + 0.7, pos.getY() + 0.35, pos.getZ() + 0.7
        );

        Vec3 hitResult = filterArea.clip(start, end).orElse(null);

        return hitResult != null;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof AugmentForgeBlockEntity nexus)) return;

        ItemStack stack = nexus.getInputItems().getStackInSlot(0);
        if (stack.isEmpty()) return;
    }

    public boolean isArmor(Item item, ArmorItem.Type type) {
        if (item instanceof ArmorItem) {
            ArmorItem armor = (ArmorItem) item;
            return armor.getType() == type;
        }
        return false;
    }
}
