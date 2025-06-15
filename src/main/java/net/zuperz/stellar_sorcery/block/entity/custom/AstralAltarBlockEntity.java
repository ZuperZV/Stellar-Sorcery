package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zuperz.stellar_sorcery.block.custom.AstralNexusBlock;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.recipes.AstralAltarRecipe;
import net.zuperz.stellar_sorcery.recipes.ModRecipes;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class AstralAltarBlockEntity extends BlockEntity {
    public int progress = 0;
    public int maxProgress = 80;
    private int prevProgress = 0;

    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private float rotation;

    public AstralAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASTRAL_ALTAR_BE.get(), pos, state);
    }

    public float getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AstralAltarBlockEntity altar) {
        altar.prevProgress = altar.progress;
        if (altar.hasRecipe()) {
            altar.progress++;
            setCRAFTING(pos, level, true);
            if (altar.progress >= altar.maxProgress) {
                altar.craftItem();
                //setCRAFTING(pos, level, false);
            }
            altar.setChanged();
        } else {
            altar.progress = 0;
            altar.setChanged();
        }

        System.out.println("progress: " + altar.progress);
        System.out.println("progress: " + altar.hasRecipe());

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos checkPos = pos.offset(dx, 0, dz);
                BlockEntity be = level.getBlockEntity(checkPos);
                if (be instanceof AstralNexusBlockEntity nexus) {
                    nexus.setSavedPos(pos);

                    nexus.progress = altar.progress;
                    nexus.maxProgress = altar.maxProgress;
                    nexus.setChanged();

                    level.sendBlockUpdated(nexus.getBlockPos(), nexus.getBlockState(), nexus.getBlockState(), 3);
                }
            }
        }
    }

    public static void setCRAFTING(BlockPos altarPos, Level level, boolean boo) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos checkPos = altarPos.offset(dx, 0, dz);
                BlockState state = level.getBlockState(checkPos);
                BlockEntity be = level.getBlockEntity(checkPos);

                if (be instanceof AstralNexusBlockEntity nexus) {
                    if (boo) {
                        if (nexus.craftingStartTime == -1) {
                            nexus.craftingStartTime = level.getGameTime();
                            nexus.setChanged();
                        }
                    } else {
                        nexus.craftingStartTime = -1;
                        nexus.setChanged();
                    }
                }
            }
        }
    }

    public boolean hasRecipe() {
        if (level == null) return false;

        System.out.println("Checking for AstralAltarRecipe at " + getBlockPos());
        System.out.println("Inventory input: " + inventory.getStackInSlot(0));

        // Laver et simpelt container med vores input
        SimpleContainer inventoryContainer = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventoryContainer.setItem(i, inventory.getStackInSlot(i));
        }

        // Finder en recipe, hvis der er en
        Optional<RecipeHolder<AstralAltarRecipe>> recipeOpt = level.getRecipeManager()
                .getRecipeFor(ModRecipes.ASTRAL_ALTAR_RECIPE_TYPE.get(), getRecipeInput(inventoryContainer), level);

        if (recipeOpt.isEmpty()) return false;

        AstralAltarRecipe altarRecipe = recipeOpt.get().value();
        ItemStack inputStack = inventory.getStackInSlot(0);

        // Tjekker om input matcher moldIngredient
        if (!altarRecipe.moldIngredient.test(inputStack)) return false;

        List<Ingredient> ingredientsToMatch = altarRecipe.additionalIngredients.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // SCANNER efter AstralNexus-blocks i samme område som i craftItem()
        boolean allMatched = true;

        for (Ingredient ingredient : ingredientsToMatch) {
            boolean matched = false;

            outer:
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0) continue;

                    BlockPos checkPos = worldPosition.offset(dx, 0, dz);
                    BlockEntity be = level.getBlockEntity(checkPos);

                    if (!(be instanceof AstralNexusBlockEntity nexus)) continue;

                    for (int slot = 0; slot < nexus.inventory.getSlots(); slot++) {
                        ItemStack stack = nexus.inventory.getStackInSlot(slot);
                        if (!stack.isEmpty() && ingredient.test(stack)) {
                            matched = true;
                            break outer;
                        }
                    }
                }
            }

            if (!matched) {
                allMatched = false;
                break;
            }
        }

        return allMatched;
    }

    public void craftItem() {
        Level level = this.level;
        if (level == null) return;

        SimpleContainer inventoryContainer = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventoryContainer.setItem(i, inventory.getStackInSlot(i));
        }

        Optional<RecipeHolder<AstralAltarRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.ASTRAL_ALTAR_RECIPE_TYPE.get(), getRecipeInput(inventoryContainer), level);

        if (recipe.isEmpty()) return;

        AstralAltarRecipe altarRecipe = recipe.get().value();
        ItemStack inputStack = inventory.getStackInSlot(0);

        if (!altarRecipe.moldIngredient.test(inputStack)) return;

        Map<Ingredient, MatchedItem> matchedIngredientSources = new HashMap<>();
        List<Ingredient> ingredientsToMatch = altarRecipe.additionalIngredients.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        boolean allMatched = true;

        for (Ingredient ingredient : ingredientsToMatch) {
            boolean matched = false;

            outer:
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0) continue;

                    BlockPos checkPos = worldPosition.offset(dx, 0, dz);
                    BlockEntity be = level.getBlockEntity(checkPos);

                    if (!(be instanceof AstralNexusBlockEntity nexus)) continue;

                    for (int slot = 0; slot < nexus.inventory.getSlots(); slot++) {
                        ItemStack stack = nexus.inventory.getStackInSlot(slot);
                        if (!stack.isEmpty() && ingredient.test(stack)) {
                            matchedIngredientSources.put(ingredient, new MatchedItem(nexus, slot));
                            matched = true;
                            break outer;
                        }
                    }
                }
            }

            if (!matched) {
                allMatched = false;
                break;
            }
        }

        if (allMatched) {
            inventory.extractItem(0, 1, false);
            for (MatchedItem matched : matchedIngredientSources.values()) {
                matched.nexus.inventory.extractItem(matched.slot, 1, false);
            }
            inventory.setStackInSlot(0, altarRecipe.output.copy());
        }
    }

    private static class RecipeMatchResult {
        public final AstralAltarRecipe recipe;
        public final Map<Ingredient, MatchedItem> matchedIngredients;

        public RecipeMatchResult(AstralAltarRecipe recipe, Map<Ingredient, MatchedItem> matchedIngredients) {
            this.recipe = recipe;
            this.matchedIngredients = matchedIngredients;
        }
    }

    private static class MatchedItem {
        public final AstralNexusBlockEntity nexus;
        public final int slot;

        public MatchedItem(AstralNexusBlockEntity nexus, int slot) {
            this.nexus = nexus;
            this.slot = slot;
        }
    }


    private boolean extractMatchingItem(int slot, Ingredient ingredient) {
        ItemStack stack = inventory.getStackInSlot(slot);

        if (ingredient.test(stack)) {
            inventory.extractItem(slot, 1, false);
            return true;
        }
        return false;
    }

    private RecipeInput getRecipeInput(SimpleContainer inventory) {
        return new RecipeInput() {
            @Override
            public ItemStack getItem(int index) {
                return inventory.getItem(index).copy();
            }

            @Override
            public int size() {
                return inventory.getContainerSize();
            }
        };
    }

    public void clearContents() {
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("progress", progress);
        tag.putInt("prevProgress", prevProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        prevProgress = tag.getInt("prevProgress");
    }

    public float getRenderingRotation() {
        rotation += 0.5f;
        if (rotation >= 360) {
            rotation = 0;
        }
        return rotation;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }
}