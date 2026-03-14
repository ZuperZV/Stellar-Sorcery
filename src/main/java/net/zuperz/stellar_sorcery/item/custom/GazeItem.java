package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.zuperz.stellar_sorcery.animation.ArmAnimationItem;
import net.zuperz.stellar_sorcery.screen.Helpers.IExtraSlotsProvider;

import java.util.List;

public class GazeItem extends Item implements Equipable, ArmAnimationItem {
    private final ResourceLocation texture;
    private final ResourceLocation armAnimationId;

    public GazeItem(Properties properties, ResourceLocation texture) {
        this(properties, texture, null);
    }

    public GazeItem(Properties properties, ResourceLocation texture, ResourceLocation armAnimationId) {
        super(properties);
        this.texture = texture;
        this.armAnimationId = armAnimationId;
    }

    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            return GazeItem.dispenseGaze(source, stack) ? stack : super.execute(source, stack);
        }
    };

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (!(player instanceof IExtraSlotsProvider provider)) return InteractionResultHolder.pass(heldStack);

        Container extraInventory = provider.getExtraSlots();

        ItemStack slot0 = extraInventory.getItem(IExtraSlotsProvider.GAZE_SLOT_RIGHT);
        ItemStack slot1 = extraInventory.getItem(IExtraSlotsProvider.GAZE_SLOT_LEFT);

        if (slot0.isEmpty()) {
            extraInventory.setItem(IExtraSlotsProvider.GAZE_SLOT_RIGHT, heldStack.split(1));
        } else if (slot1.isEmpty()) {
            extraInventory.setItem(IExtraSlotsProvider.GAZE_SLOT_LEFT, heldStack.split(1));
        }

        return InteractionResultHolder.sidedSuccess(heldStack, level.isClientSide());
    }

    public static boolean dispenseGaze(BlockSource source, ItemStack stack) {
        BlockPos pos = source.pos().relative(source.state().getValue(net.minecraft.world.level.block.DispenserBlock.FACING));
        List<LivingEntity> entities = source.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(pos),
                e -> e instanceof IExtraSlotsProvider
        );

        if (entities.isEmpty()) return false;

        LivingEntity target = entities.get(0);
        if (!(target instanceof IExtraSlotsProvider provider)) return false;

        Container extraInventory = provider.getExtraSlots();

        ItemStack slot0 = extraInventory.getItem(IExtraSlotsProvider.GAZE_SLOT_RIGHT);
        ItemStack slot1 = extraInventory.getItem(IExtraSlotsProvider.GAZE_SLOT_LEFT);

        if (slot0.isEmpty()) {
            extraInventory.setItem(IExtraSlotsProvider.GAZE_SLOT_RIGHT, stack.split(1));
            return true;
        } else if (slot1.isEmpty()) {
            extraInventory.setItem(IExtraSlotsProvider.GAZE_SLOT_LEFT, stack.split(1));
            return true;
        }
        return false;
    }

    @Override
    public Holder<SoundEvent> getEquipSound() {
        return null;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.MAINHAND;
    }

    public ResourceLocation getGazeTexture() {
        return texture;
    }

    @Override
    public ResourceLocation getArmAnimationId(ItemStack stack) {
        return armAnimationId;
    }
}
